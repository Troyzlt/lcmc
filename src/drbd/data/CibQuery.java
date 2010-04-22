/*
 * This file is part of DRBD Management Console by LINBIT HA-Solutions GmbH
 * written by Rasto Levrinc.
 *
 * Copyright (C) 2009, LINBIT HA-Solutions GmbH.
 *
 * DRBD Management Console is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License as published
 * by the Free Software Foundation; either version 2, or (at your option)
 * any later version.
 *
 * DRBD Management Console is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with drbd; see the file COPYING.  If not, write to
 * the Free Software Foundation, 675 Mass Ave, Cambridge, MA 02139, USA.
 */

package drbd.data;

import java.util.Map;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.ArrayList;
import org.apache.commons.collections.map.MultiKeyMap;

/**
 * This class holds data that were parsed from cib xml. This is not used in old
 * heartbeats before pacemaker.
 *
 * @author Rasto Levrinc
 * @version $Id$
 *
 */
public class CibQuery {
    /** Serial version UID. */
    private static final long serialVersionUID = 1L;
    /** crm_config / global config data. */
    private Map<String, String> crmConfig = new HashMap<String, String>();
    /** Map with resources and its parameters. */
    private Map<String, Map<String, String>> parameters =
                                    new HashMap<String, Map<String, String>>();
    /** Map with resources and parameters nvpair ids. */
    private Map<String, Map<String, String>> parametersNvpairsIds;
    /** Map with resource type. */
    private Map<String, ResourceAgent> resourceType;
    /** Map with resource instance_attributes id. */
    private Map<String, String> resourceInstanceAttrId;
    /** Colocation rsc map. */
    private Map<String, List<CRMXML.ColocationData>> colocationRsc =
                      new LinkedHashMap<String, List<CRMXML.ColocationData>>();
    /** Colocation id map. */
    private Map<String, CRMXML.ColocationData> colocationId =
                      new LinkedHashMap<String, CRMXML.ColocationData>();
    /** Order rsc map. */
    private Map<String, List<CRMXML.OrderData>> orderRsc =
                      new LinkedHashMap<String, List<CRMXML.OrderData>>();
    /** Order id map. */
    private Map<String, CRMXML.OrderData> orderId =
                      new LinkedHashMap<String, CRMXML.OrderData>();
    /** Node parameters map. */
    private MultiKeyMap nodeParameters;
    /** Location map. */
    private Map<String, Map<String, HostLocation>> location =
                              new HashMap<String, Map<String, HostLocation>>();
    /** Locations id map. */
    private Map<String, List<String>> locationsId =
                                           new HashMap<String, List<String>>();
    /** Location id to host location map. */
    private Map<String, HostLocation> idToLocation =
                                           new HashMap<String, HostLocation>();
    /** Map from resource and host to location id. */
    private MultiKeyMap resHostToLocId = new MultiKeyMap();
    /** Operations map. */
    private MultiKeyMap operations = new MultiKeyMap();
    /** Operations refs map. */
    private Map<String, String> operationsRefs = new HashMap<String, String>();
    /** Meta attrs id map. */
    private Map<String, String> metaAttrsId = new HashMap<String, String>();
    /** Metaattrs refs map. */
    private Map<String, String> metaAttrsRefs = new HashMap<String, String>();
    /** Operations id map. */
    private Map<String, String> operationsId = new HashMap<String, String>();
    /** "op" tag id map. */
    private Map<String, Map<String, String>> resOpIds =
                                    new HashMap<String, Map<String, String>>();
    /** If node is online. */
    private Map<String, String> nodeOnline = new HashMap<String, String>();
    /** Group to resources map. */
    private Map<String, List<String>> groupsToResources =
                                           new HashMap<String, List<String>>();
    /** Clone to its resource map. */
    private Map<String, String> cloneToResource =
                                               new HashMap<String, String>();
    /** List of mater resources. */
    private List<String> masterList = new ArrayList<String>();
    /** Designated co-ordinator. */
    private String dc = null;
    /** Map from nodename and resource to the fail-count. */
    private MultiKeyMap failed = new MultiKeyMap();
    /** rsc_defaults meta attributes id. */
    private String rscDefaultsId = null;
    /** rsc_defaults parameters with values. */
    private Map<String, String> rscDefaultsParams =
                                                 new HashMap<String, String>();
    /** rsc_defaults parameters with ids. */
    private Map<String, String> rscDefaultsParamsNvpairIds =
                                                 new HashMap<String, String>();
    /** op_defaults parameters with values. */
    private Map<String, String> opDefaultsParams =
                                                 new HashMap<String, String>();

    /**
     * Sets crm config map.
     */
    public final void setCrmConfig(final Map<String, String> crmConfig) {
        this.crmConfig = crmConfig;
    }

    /**
     * Returns crm config.
     */
    public final Map<String, String> getCrmConfig() {
        return crmConfig;
    }

    /**
     * Sets parameters map, with the first key being the resource id and the
     * second key being the parameter name.
     */
    public final void setParameters(
                           final Map<String, Map<String, String>> parameters) {
        this.parameters = parameters;
    }

    /**
     * Returns the parameters map.
     */
    public final Map<String, Map<String, String>> getParameters() {
        return parameters;
    }

    /**
     * Sets the parameters nvpairs id map, with the first key being the
     * resource id and the second key being the parameter name.
     */
    public final void setParametersNvpairsIds(
                  final Map<String, Map<String, String>> parametersNvpairsIds) {
        this.parametersNvpairsIds = parametersNvpairsIds;
    }

    /**
     * Returns the parameters nvpairs id map.
     */
    public final Map<String, Map<String, String>> getParametersNvpairsIds() {
        return parametersNvpairsIds;
    }

    /**
     * Sets the resource type map.
     */
    public final void setResourceType(
                            final Map<String, ResourceAgent> resourceType) {
        this.resourceType = resourceType;
    }

    /**
     * Returns the resource type map.
     */
    public final Map<String, ResourceAgent> getResourceType() {
        return resourceType;
    }

    /**
     * Sets the resource instance_attributes id map.
     */
    public final void setResourceInstanceAttrId(
                            final Map<String, String> resourceInstanceAttrId) {
        this.resourceInstanceAttrId = resourceInstanceAttrId;
    }

    /**
     * Returns the resource instance_attributes map.
     */
    public final Map<String, String> getResourceInstanceAttrId() {
        return resourceInstanceAttrId;
    }


    /**
     * Sets the colocation map with one resource as a key and list of
     * colocation constraints.
     */
    public final void setColocationRsc(
              final Map<String, List<CRMXML.ColocationData>> colocationRsc) {
        this.colocationRsc = colocationRsc;
    }

    /**
     * Sets the colocation map with resource id as a key with colocation data
     * object.
     */
    public final void setColocationId(
                  final Map<String, CRMXML.ColocationData> colocationId) {
        this.colocationId = colocationId;
    }

    /**
     * Returns colocation id map.
     */
    public final Map<String, CRMXML.ColocationData> getColocationId() {
        return colocationId;
    }

    /**
     * Returns colocation rsc map.
     */
    public final Map<String, List<CRMXML.ColocationData>> getColocationRsc() {
        return colocationRsc;
    }

    /**
     * Sets the colocation map with one resource as a key and list of
     * colocation constraints.
     */
    public final void setOrderRsc(
              final Map<String, List<CRMXML.OrderData>> orderRsc) {
        this.orderRsc = orderRsc;
    }

    /**
     * Sets the order map with resource id as a key with order data
     * object.
     */
    public final void setOrderId(
                  final Map<String, CRMXML.OrderData> orderId) {
        this.orderId = orderId;
    }

    /**
     * Returns id rsc map.
     */
    public final Map<String, CRMXML.OrderData> getOrderId() {
        return orderId;
    }
    /**
     * Returns order rsc map.
     */
    public final Map<String, List<CRMXML.OrderData>> getOrderRsc() {
        return orderRsc;
    }

    /**
     * Sets node parameters map.
     */
    public final void setNodeParameters(final MultiKeyMap nodeParameters) {
        this.nodeParameters = nodeParameters;
    }

    /**
     * Gets node parameters map.
     */
    public final MultiKeyMap getNodeParameters() {
        return nodeParameters;
    }

    /**
     * Sets location map.
     */
    public final void setLocation(
                       final Map<String, Map<String, HostLocation>> location) {
        this.location = location;
    }

    /**
     * Returns location map.
     */
    public final Map<String, Map<String, HostLocation>> getLocation() {
        return location;
    }

    /**
     * Sets locations id map.
     */
    public final void setLocationsId(
                                final Map<String, List<String>> locationsId) {
        this.locationsId = locationsId;
    }

    /**
     * Returns locations id map.
     */
    public final Map<String, List<String>> getLocationsId() {
        return locationsId;
    }

    /**
     * Sets map from location id to the score.
     */
    public final void setLocationMap(
                            final Map<String, HostLocation> idToLocation) {
        this.idToLocation = idToLocation;
    }

    /**
     * Returns map from location id to the score.
     */
    public final Map<String, HostLocation> getLocationMap() {
        return idToLocation;
    }

    /**
     * Sets map from resource and host to the location id.
     */
    public final void setResHostToLocId(final MultiKeyMap resHostToLocId) {
        this.resHostToLocId = resHostToLocId;
    }

    /**
     * Returns map from resource and host to the location id.
     */
    public final MultiKeyMap getResHostToLocId() {
        return resHostToLocId;
    }

    /**
     * Sets operations map.
     */
    public final void setOperations(final MultiKeyMap operations) {
        this.operations = operations;
    }

    /**
     * Sets operations refs map.
     */
    public final void setOperationsRefs(
                                final Map<String, String> operationsRefs) {
        this.operationsRefs = operationsRefs;
    }

    /**
     * Sets meta attrs refs map.
     */
    public final void setMetaAttrsRefs(
                                final Map<String, String> metaAttrsRefs) {
        this.metaAttrsRefs = metaAttrsRefs;
    }

    /**
     * Sets meta attrs id map.
     */
    public final void setMetaAttrsId(final Map<String, String> metaAttrsId) {
        this.metaAttrsId = metaAttrsId;
    }

    /**
     * Returns meta attrs id map.
     */
    public final Map<String, String> getMetaAttrsId() {
        return metaAttrsId;
    }

    /**
     * Returns meta attrs refs map.
     */
    public final Map<String, String> getMetaAttrsRefs() {
        return metaAttrsRefs;
    }


    /**
     * Returns operations map.
     */
    public final MultiKeyMap getOperations() {
        return operations;
    }

    /**
     * Returns operations refs map.
     */
    public final Map<String, String> getOperationsRefs() {
        return operationsRefs;
    }

    /**
     * Sets operations id map.
     */
    public final void setOperationsId(final Map<String, String> operationsId) {
        this.operationsId = operationsId;
    }

    /**
     * Returns operations id map.
     */
    public final Map<String, String> getOperationsId() {
        return operationsId;
    }

    /**
     * Sets "op" tag id map.
     */
    public final void setResOpIds(
                            final Map<String, Map<String, String>> resOpIds) {
        this.resOpIds = resOpIds;
    }

    /**
     * Returns "op" tag id map.
     */
    public final Map<String, Map<String, String>> getResOpIds() {
        return resOpIds;
    }

    /**
     * Sets map with nodes and if they ore online.
     */
    public final void setNodeOnline(final Map<String, String> nodeOnline) {
        this.nodeOnline = nodeOnline;
    }

    /**
     * Gets map whith nodes and if they are online.
     */
    public final Map<String, String> getNodeOnline() {
        return nodeOnline;
    }

    /**
     * Sets the groups to resources map.
     */
    public final void setGroupsToResources(
                           final Map<String, List<String>> groupsToResources) {
        this.groupsToResources = groupsToResources;
    }

    /**
     * Gets the groups to resources map.
     */
    public final Map<String, List<String>> getGroupsToResources() {
        return groupsToResources;
    }

    /**
     * Sets the clone to resource map.
     */
    public final void setCloneToResource(
                           final Map<String, String> cloneToResource) {
        this.cloneToResource = cloneToResource;
    }

    /**
     * Gets the clone to resource map.
     */
    public final Map<String, String> getCloneToResource() {
        return cloneToResource;
    }

    /**
     * Sets the master list.
     */
    public final void setMasterList(final List<String> masterList) {
        this.masterList = masterList;
    }

    /**
     * Gets the master list.
     */
    public final List<String> getMasterList() {
        return masterList;
    }


    /**
     * Sets the designated co-ordinator.
     */
    public final void setDC(final String dc) {
        this.dc = dc;
    }

    /**
     * Gets the designated co-ordinator.
     */
    public final String getDC() {
        return dc;
    }

    /**
     * Sets failed map.
     */
    public final void setFailed(final MultiKeyMap failed) {
        this.failed = failed;
    }

    /**
     * Returns failed map.
     */
    public final MultiKeyMap getFailed() {
        return failed;
    }

    /**
     * Returns fail-count. It can be "INFINITY"
     */
     public final String getFailCount(final String node, final String res) {
         return (String) failed.get(node, res);
     }

    /** Sets rsc_defaults meta attributes id. */
    public final void setRscDefaultsId(final String rscDefaultsId) {
        this.rscDefaultsId = rscDefaultsId;
    }

    /** Gets rsc_defaults meta attributes id. */
    public final String getRscDefaultsId() {
        return rscDefaultsId;
    }

    /** Sets rsc_defaults parameters with values. */
    public final void setRscDefaultsParams(
                                final Map<String, String> rscDefaultsParams) {
        this.rscDefaultsParams = rscDefaultsParams;
    }

    /** Gets rsc_defaults parameters with values. */
    public final Map<String, String> getRscDefaultsParams() {
        return rscDefaultsParams;
    }

    /** Sets rsc_defaults parameters with ids. */
    public final void setRscDefaultsParamsNvpairIds(
                        final Map<String, String> rscDefaultsParamsNvpairIds) {
        this.rscDefaultsParamsNvpairIds = rscDefaultsParamsNvpairIds;
    }

    /** Gets rsc_defaults parameters with ids. */
    public final Map<String, String> getRscDefaultsParamsNvpairIds() {
        return rscDefaultsParamsNvpairIds;
    }

    /** Sets op_defaults parameters with values. */
    public final void setOpDefaultsParams(
                                final Map<String, String> opDefaultsParams) {
        this.opDefaultsParams = opDefaultsParams;
    }

    /** Gets op_defaults parameters with values. */
    public final Map<String, String> getOpDefaultsParams() {
        return opDefaultsParams;
    }
}
