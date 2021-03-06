/*
 * Copyright 2017 Google, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *    http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

package com.netflix.spinnaker.clouddriver.kubernetes.v2.caching.view.model;

import com.netflix.spinnaker.cats.cache.CacheData;
import com.netflix.spinnaker.clouddriver.kubernetes.v2.caching.Keys;
import com.netflix.spinnaker.clouddriver.kubernetes.v2.caching.agent.KubernetesCacheDataConverter;
import com.netflix.spinnaker.clouddriver.kubernetes.v2.description.KubernetesManifest;
import com.netflix.spinnaker.clouddriver.model.Instance;
import com.netflix.spinnaker.clouddriver.model.ServerGroup;
import lombok.Data;
import lombok.EqualsAndHashCode;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@EqualsAndHashCode(callSuper = true)
@Data
@Slf4j
public class KubernetesV2ServerGroup extends ManifestBasedModel implements ServerGroup {
  Boolean disabled;
  Long createdTime;
  Set<String> zones = new HashSet<>();
  Set<Instance> instances = new HashSet<>();
  Set<String> loadBalancers = new HashSet<>();
  Set<String> securityGroups = new HashSet<>();
  Map<String, Object> launchConfig = new HashMap<>();
  InstanceCounts instanceCounts;
  Capacity capacity = new Capacity();
  ImageSummary imageSummary;
  ImagesSummary imagesSummary;
  KubernetesManifest manifest;
  Keys.InfrastructureCacheKey key;

  @Override
  public Boolean isDisabled() {
    return disabled;
  }

  private KubernetesV2ServerGroup(KubernetesManifest manifest, String key, List<KubernetesV2Instance> instances) {
    this.manifest = manifest;
    this.key = (Keys.InfrastructureCacheKey) Keys.parseKey(key).get();
    this.instances = new HashSet<>(instances);
  }

  public static KubernetesV2ServerGroup fromCacheData(CacheData cd, List<CacheData> instanceData) {
    if (cd == null) {
      return null;
    }

    if (instanceData == null) {
      instanceData = new ArrayList<>();
    }

    KubernetesManifest manifest = KubernetesCacheDataConverter.getManifest(cd);

    if (manifest == null) {
      log.warn("Cache data {} inserted without a manifest", cd.getId());
      return null;
    }

    List<KubernetesV2Instance> instances = instanceData.stream()
        .map(KubernetesV2Instance::fromCacheData)
        .collect(Collectors.toList());

    return new KubernetesV2ServerGroup(manifest, cd.getId(), instances);
  }
}
