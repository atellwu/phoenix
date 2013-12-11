/**
 * Project: phoenix-load-balancer
 * 
 * File Created at Oct 30, 2013
 * 
 */
package com.dianping.phoenix.lb.visitor;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.dianping.phoenix.lb.constant.Constants;
import com.dianping.phoenix.lb.model.entity.Directive;
import com.dianping.phoenix.lb.model.entity.Location;
import com.dianping.phoenix.lb.model.entity.Pool;
import com.dianping.phoenix.lb.model.entity.SlbModelTree;
import com.dianping.phoenix.lb.model.entity.VirtualServer;
import com.dianping.phoenix.lb.utils.AdvEqualsBuilder;
import com.dianping.phoenix.lb.visitor.VirtualServerComparisionVisitor.ComparisionResult;

/**
 * @author Leo Liang
 * 
 */
public class VirtualServerComparisionVisitor extends AbstractVisitor<ComparisionResult> {

    public static class ComparisionResult {
        private List<Pool> addedPools    = new ArrayList<Pool>();
        private List<Pool> deletedPools  = new ArrayList<Pool>();
        private List<Pool> modifiedPools = new ArrayList<Pool>();

        public boolean needReload() {
            return addedPools.isEmpty() && modifiedPools.isEmpty() && deletedPools.isEmpty();
        }

        public List<Pool> getAddedPools() {
            return addedPools;
        }

        public List<Pool> getDeletedPools() {
            return deletedPools;
        }

        public List<Pool> getModifiedPools() {
            return modifiedPools;
        }

        public void addAddedPools(Pool pool) {
            this.addedPools.add(pool);
        }

        public void addModifiedPools(Pool pool) {
            this.modifiedPools.add(pool);
        }

        public void addDeletedPools(Pool pool) {
            this.deletedPools.add(pool);
        }

    }

    private VirtualServer     baseVs;
    private Map<String, Pool> basePools;

    public VirtualServerComparisionVisitor(VirtualServer baseVs, Map<String, Pool> basePools) {
        result = new ComparisionResult();
        this.baseVs = baseVs;
        this.basePools = basePools;
    }

    public void visitSlbModelTree(SlbModelTree slbModelTree) {

        VirtualServer vs = slbModelTree.findVirtualServer(baseVs.getName());
        if (AdvEqualsBuilder.reflectionEquals(baseVs, vs, true, null, new String[] { "m_creationDate",
                "m_lastModifiedDate", "m_version", "m_instances" })) {
            Set<String> usedPoolNamePrefixs = new HashSet<String>();

            usedPoolNamePrefixs.add(vs.getDefaultPoolName());

            for (Location location : vs.getLocations()) {
                for (Directive directive : location.getDirectives()) {
                    if (Constants.DIRECTIVE_PROXY_PASS.equals(directive.getType())) {
                        usedPoolNamePrefixs
                                .add(directive.getDynamicAttribute(Constants.DIRECTIVE_PROXY_PASS_POOL_NAME));
                    }
                }
            }

            comparePools(usedPoolNamePrefixs, slbModelTree.getPools());
        }

    }

    private void comparePools(Set<String> usedPoolNamePrefixs, Map<String, Pool> pools) {
        for (Pool basePool : basePools.values()) {
            if (usedPoolNamePrefixs.contains(getPoolNamePrefix(basePool.getName()))) {
                if (!pools.containsKey(basePool.getName())) {
                    result.addDeletedPools(basePool);
                } else {
                    if (!AdvEqualsBuilder.reflectionEquals(basePool, pools.get(basePool.getName()), true, null,
                            new String[] { "m_creationDate", "m_lastModifiedDate" })) {
                        result.addModifiedPools(pools.get(basePool.getName()));
                    }
                }
            }
        }

        for (Pool pool : pools.values()) {
            if (usedPoolNamePrefixs.contains(getPoolNamePrefix(pool.getName()))) {
                if (!basePools.containsKey(pool.getName())) {
                    result.addAddedPools(pool);
                } else {
                    if (!result.getModifiedPools().contains(pool)
                            && !AdvEqualsBuilder.reflectionEquals(pool, basePools.get(pool.getName()), new String[] {
                                    "m_creationDate", "m_lastModifiedDate" })) {
                        result.addModifiedPools(pool);
                    }
                }
            }
        }
    }

    private String getPoolNamePrefix(String poolName) {
        int prefixPos = poolName.indexOf("@");
        if (prefixPos >= 0) {
            return poolName.substring(0, prefixPos);
        } else {
            return poolName;
        }
    }
}
