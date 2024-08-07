/*
 * Licensed to the Apache Software Foundation (ASF) under one
 * or more contributor license agreements.  See the NOTICE file
 * distributed with this work for additional information
 * regarding copyright ownership.  The ASF licenses this file
 * to you under the Apache License, Version 2.0 (the
 * "License"); you may not use this file except in compliance
 * with the License.  You may obtain a copy of the License at
 *
 *   http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing,
 * software distributed under the License is distributed on an
 * "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
 * KIND, either express or implied.  See the License for the
 * specific language governing permissions and limitations
 * under the License.
 */
package org.apache.pulsar.client.admin.internal;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import org.apache.pulsar.client.admin.Properties;
import org.apache.pulsar.client.admin.PulsarAdminException;
import org.apache.pulsar.client.admin.Tenants;
import org.apache.pulsar.client.api.Authentication;
import org.apache.pulsar.common.policies.data.TenantInfo;
import org.apache.pulsar.common.policies.data.TenantInfoImpl;

@SuppressWarnings("deprecation")
public class TenantsImpl extends BaseResource implements Tenants, Properties {
    private final WebTarget adminTenants;

    public TenantsImpl(WebTarget web, Authentication auth, long requestTimeoutMs) {
        super(auth, requestTimeoutMs);
        adminTenants = web.path("/admin/v2/tenants");
    }

    @Override
    public List<String> getTenants() throws PulsarAdminException {
        return sync(() -> getTenantsAsync());
    }

    @Override
    public CompletableFuture<List<String>> getTenantsAsync() {
        return asyncGetRequest(this.adminTenants, new FutureCallback<List<String>>(){});
    }

    @Override
    public TenantInfo getTenantInfo(String tenant) throws PulsarAdminException {
        return sync(() -> getTenantInfoAsync(tenant));
    }

    @Override
    public CompletableFuture<TenantInfo> getTenantInfoAsync(String tenant) {
        WebTarget path = adminTenants.path(tenant);
        return asyncGetRequest(path, new FutureCallback<TenantInfoImpl>(){})
                .thenApply(tenantInfo -> tenantInfo);
    }

    @Override
    public void createTenant(String tenant, TenantInfo config) throws PulsarAdminException {
        sync(() -> createTenantAsync(tenant, config));
    }

    @Override
    public CompletableFuture<Void> createTenantAsync(String tenant, TenantInfo config) {
        WebTarget path = adminTenants.path(tenant);
        return asyncPutRequest(path, Entity.entity(config, MediaType.APPLICATION_JSON));
    }

    @Override
    public void updateTenant(String tenant, TenantInfo config) throws PulsarAdminException {
        sync(() -> updateTenantAsync(tenant, config));
    }

    @Override
    public CompletableFuture<Void> updateTenantAsync(String tenant, TenantInfo config) {
        WebTarget path = adminTenants.path(tenant);
        return asyncPostRequest(path, Entity.entity((TenantInfoImpl) config, MediaType.APPLICATION_JSON));
    }

    @Override
    public void deleteTenant(String tenant) throws PulsarAdminException {
        sync(() -> deleteTenantAsync(tenant));
    }

    @Override
    public void deleteTenant(String tenant, boolean force) throws PulsarAdminException {
        sync(() -> deleteTenantAsync(tenant, force));
    }

    @Override
    public CompletableFuture<Void> deleteTenantAsync(String tenant) {
        return deleteTenantAsync(tenant, false);
    }

    @Override
    public CompletableFuture<Void> deleteTenantAsync(String tenant, boolean force) {
        WebTarget path = adminTenants.path(tenant);
        path = path.queryParam("force", force);
        return asyncDeleteRequest(path);
    }

    // Compat method names

    @Override
    public void createProperty(String tenant, TenantInfo config) throws PulsarAdminException {
        createTenant(tenant, config);
    }

    @Override
    public void updateProperty(String tenant, TenantInfo config) throws PulsarAdminException {
        updateTenant(tenant, config);
    }

    @Override
    public void deleteProperty(String tenant) throws PulsarAdminException {
        deleteTenant(tenant);
    }

    @Override
    public List<String> getProperties() throws PulsarAdminException {
        return getTenants();
    }

    @Override
    public TenantInfo getPropertyAdmin(String tenant) throws PulsarAdminException {
        return getTenantInfo(tenant);
    }

    public WebTarget getWebTarget() {
        return adminTenants;
    }
}
