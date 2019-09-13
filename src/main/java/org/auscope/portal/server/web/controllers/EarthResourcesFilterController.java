package org.auscope.portal.server.web.controllers;

import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.net.URLDecoder;
import java.util.Hashtable;

import javax.servlet.http.HttpServletResponse;

import org.auscope.portal.core.configuration.ServiceConfiguration;
import org.auscope.portal.core.configuration.ServiceConfigurationItem;
import org.auscope.portal.core.server.controllers.BasePortalController;
import org.auscope.portal.core.services.methodmakers.filter.FilterBoundingBox;
import org.auscope.portal.core.services.responses.wfs.WFSCountResponse;
import org.auscope.portal.core.services.responses.wfs.WFSResponse;
import org.auscope.portal.core.uifilter.GenericFilterAdapter;
import org.auscope.portal.core.util.FileIOUtil;
import org.auscope.portal.core.util.SLDLoader;
import org.auscope.portal.server.web.controllers.downloads.EarthResourcesDownloadController;
import org.auscope.portal.server.web.service.MineralOccurrenceService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * Controller that handles all Earth Resource related requests
 * <p>
 * It handles the following WFS features:
 * <ul>
 * <li>Mine</li>
 * <li>Mineral Occurrence</li>
 * <li>Mining Activity</li>
 * </ul>
 * </p>
 *
 * @author Jarek Sanders
 * @author Josh Vote
 */
@Controller
public class EarthResourcesFilterController extends BasePortalController {

    // ----------------------------------------------------- Instance variables

    private MineralOccurrenceService mineralOccurrenceService;

    private ServiceConfiguration serviceConfig;
    // ----------------------------------------------------------- Constructors

    @Autowired
    public EarthResourcesFilterController(MineralOccurrenceService mineralOccurrenceService, 
            ServiceConfiguration serviceConfig) {
        this.mineralOccurrenceService = mineralOccurrenceService;
        this.serviceConfig = serviceConfig;
    }

    // ------------------------------------------- Property Setters and Getters

    /**
     * Handles the Earth Resource Mine filter queries. (If the bbox elements are specified, they will limit the output response to 200 records implicitly)
     *
     * @param serviceUrl
     *            the url of the service to query
     * @param mineName
     *            the name of the mine to query for
     * @param request
     *            the HTTP client request
     * @return a WFS response converted into KML
     * @throws Exception
     */
    @RequestMapping("/doMineFilter.do")
    public ModelAndView doMineFilter(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam("mineName") String mineName,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {

        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            WFSResponse response = this.mineralOccurrenceService.getMinesGml(serviceUrl, mineName, bbox,
                    maxFeatures);

            return generateNamedJSONResponseMAV(true, "gml", response.getData(), response.getMethod());
        } catch (Exception e) {
            log.warn(String.format("Error performing filter for '%1$s': %2$s", serviceUrl, e));
            log.debug("Exception: ", e);
            return this.generateExceptionResponse(e, serviceUrl);
        }
    }

    /**
     * Handles getting the count of the Earth Resource Mine filter queries. (If the bbox elements are specified, they will limit the output response to 200
     * records implicitly)
     *
     * @param serviceUrl
     *            the url of the service to query
     * @param mineName
     *            the name of the mine to query for
     * @param request
     *            the HTTP client request
     * @return a WFS response converted into KML
     * @throws Exception
     */
    @RequestMapping("/doMineFilterCount.do")
    public ModelAndView doMineFilterCount(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam(required = false, value = "mineName") String mineName,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {

        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            WFSCountResponse response = this.mineralOccurrenceService.getMinesCount(serviceUrl, mineName, bbox,
                    maxFeatures);
            return generateJSONResponseMAV(true, new Integer(response.getNumberOfFeatures()), "");
        } catch (Exception e) {
            log.warn(String.format("Error performing filter for '%1$s': %2$s", serviceUrl, e));
            log.debug("Exception: ", e);
            return this.generateExceptionResponse(e, serviceUrl);
        }
    }

    /**
     * Handles the Earth Resource MineralOccurrence filter queries.
     *
     * @param serviceUrl
     * @param commodityName
     * @param measureType
     * @param minOreAmount
     * @param minOreAmountUOM
     * @param minCommodityAmount
     * @param minCommodityAmountUOM
     * @param request
     *            the HTTP client request
     *
     * @return a WFS response converted into KML
     * @throws Exception
     */
    @RequestMapping("/doMineralOccurrenceFilter.do")
    public ModelAndView doMineralOccurrenceFilter(
            @RequestParam(value = "serviceUrl", required = false) String serviceUrl,
            @RequestParam(value = "commodityName", required = false) String commodityName,
            @RequestParam(value = "measureType", required = false) String measureType,
            @RequestParam(value = "minOreAmount", required = false) String minOreAmount,
            @RequestParam(value = "minOreAmountUOM", required = false) String minOreAmountUOM,
            @RequestParam(value = "minCommodityAmount", required = false) String minCommodityAmount,
            @RequestParam(value = "minCommodityAmountUOM", required = false) String minCommodityAmountUOM,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {
        //The presence of a bounding box causes us to assume we will be using this GML for visualising on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            //get the mineral occurrences
            WFSResponse response = this.mineralOccurrenceService.getMineralOccurrenceGml(
                    serviceUrl,
                    commodityName,
                    measureType,
                    minOreAmount,
                    minOreAmountUOM,
                    minCommodityAmount,
                    minCommodityAmountUOM,
                    maxFeatures,
                    bbox);

            return generateNamedJSONResponseMAV(true, "gml", response.getData(), response.getMethod());
        } catch (Exception e) {
            log.warn(String.format("Error performing filter for '%1$s': %2$s", serviceUrl, e));
            log.debug("Exception: ", e);
            return this.generateExceptionResponse(e, serviceUrl);

        }
    }

    /**
     * Handles counting the results of an Earth Resource MineralOccurrence filter query.
     *
     * @param serviceUrl
     * @param commodityName
     * @param measureType
     * @param minOreAmount
     * @param minOreAmountUOM
     * @param minCommodityAmount
     * @param minCommodityAmountUOM
     * @param request
     *            the HTTP client request
     *
     * @return Returns Integer count
     * @throws Exception
     */
    @RequestMapping("/doMineralOccurrenceFilterCount.do")
    public ModelAndView doMineralOccurrenceFilterCount(
            @RequestParam(value = "serviceUrl", required = false) String serviceUrl,
            @RequestParam(value = "commodityName", required = false) String commodityName,
            @RequestParam(value = "measureType", required = false) String measureType,
            @RequestParam(value = "minOreAmount", required = false) String minOreAmount,
            @RequestParam(value = "minOreAmountUOM", required = false) String minOreAmountUOM,
            @RequestParam(value = "minCommodityAmount", required = false) String minCommodityAmount,
            @RequestParam(value = "minCommodityAmountUOM", required = false) String minCommodityAmountUOM,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {
        //The presence of a bounding box causes us to assume we will be using this GML for visualising on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            //get the mineral occurrences
            WFSCountResponse response = this.mineralOccurrenceService.getMineralOccurrenceCount(
                    serviceUrl,
                    commodityName,
                    measureType,
                    minOreAmount,
                    minOreAmountUOM,
                    minCommodityAmount,
                    minCommodityAmountUOM,
                    maxFeatures,
                    bbox);

            return generateJSONResponseMAV(true, new Integer(response.getNumberOfFeatures()), "");
        } catch (Exception e) {
            log.warn(String.format("Error performing filter for '%1$s': %2$s", serviceUrl, e));
            log.debug("Exception: ", e);
            return this.generateExceptionResponse(e, serviceUrl);

        }
    }

    /**
     * Handles Mining Activity filter queries Returns WFS response converted into KML.
     *
     * @param serviceUrl
     * @param mineName
     * @param startDate
     * @param endDate
     * @param oreProcessed
     * @param producedMaterial
     * @param cutOffGrade
     * @param production
     * @param request
     * @return the KML response
     * @throws Exception
     */
    @RequestMapping("/doMiningActivityFilter.do")
    public ModelAndView doMiningActivityFilter(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam(required = false, value = "mineName", defaultValue = "") String mineName,
            @RequestParam(required = false, value = "startDate", defaultValue = "") String startDate,
            @RequestParam(required = false, value = "endDate", defaultValue = "") String endDate,
            @RequestParam(required = false, value = "oreProcessed", defaultValue = "") String oreProcessed,
            @RequestParam(required = false, value = "producedMaterial", defaultValue = "") String producedMaterial,
            @RequestParam(required = false, value = "cutOffGrade", defaultValue = "") String cutOffGrade,
            @RequestParam(required = false, value = "production", defaultValue = "") String production,
            @RequestParam(required = false, value = "bbox", defaultValue = "") String bboxJson,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception
    {
        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            // Get the mining activities
            WFSResponse response = this.mineralOccurrenceService.getMiningActivityGml(serviceUrl
                    , mineName
                    , startDate
                    , endDate
                    , oreProcessed
                    , producedMaterial
                    , cutOffGrade
                    , production
                    , maxFeatures
                    , bbox);

            return generateNamedJSONResponseMAV(true, "gml", response.getData(), response.getMethod());
        } catch (Exception e) {
            log.warn(String.format("Error performing filter for '%1$s': %2$s", serviceUrl, e));
            log.debug("Exception: ", e);
            return this.generateExceptionResponse(e, serviceUrl);
        }
    }

    /**
     * Handles counting the number Mining Activities matched by a filter Returns Integer count
     *
     * @param serviceUrl
     * @param mineName
     * @param startDate
     * @param endDate
     * @param oreProcessed
     * @param producedMaterial
     * @param cutOffGrade
     * @param production
     * @param request
     * @return Returns Integer count
     * @throws Exception
     */
    @RequestMapping("/doMiningActivityFilterCount.do")
    public ModelAndView doMiningActivityFilterCount(
            @RequestParam("serviceUrl") String serviceUrl,
            @RequestParam(required = false, value = "mineName", defaultValue = "") String mineName,
            @RequestParam(required = false, value = "startDate", defaultValue = "") String startDate,
            @RequestParam(required = false, value = "endDate", defaultValue = "") String endDate,
            @RequestParam(required = false, value = "oreProcessed", defaultValue = "") String oreProcessed,
            @RequestParam(required = false, value = "producedMaterial", defaultValue = "") String producedMaterial,
            @RequestParam(required = false, value = "cutOffGrade", defaultValue = "") String cutOffGrade,
            @RequestParam(required = false, value = "production", defaultValue = "") String production,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {

        //The presence of a bounding box causes us to assume we will be using this GML for visualizing on a map
        //This will in turn limit the number of points returned to 200
        FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(bboxJson);

        try {
            // Get the mining activities
            WFSCountResponse response = this.mineralOccurrenceService.getMiningActivityCount(serviceUrl
                    , mineName
                    , startDate
                    , endDate
                    , oreProcessed
                    , producedMaterial
                    , cutOffGrade
                    , production
                    , maxFeatures
                    , bbox);

            return generateJSONResponseMAV(true, new Integer(response.getNumberOfFeatures()), "");
        } catch (Exception e) {
            log.warn(String.format("Error performing filter for '%1$s': %2$s", serviceUrl, e));
            log.debug("Exception: ", e);
            return this.generateExceptionResponse(e, serviceUrl);
        }
    }

    /**
     * Handles Mining Activity Style request queries Returns WFS response converted into KML.
     *
     * @param mineName
     * @param startDate
     * @param endDate
     * @param oreProcessed
     * @param producedMaterial
     * @param cutOffGrade
     * @param production
     * @param bbox
     * @param maxFeatures
     */
    @RequestMapping("/doMiningActivityFilterStyle.do")
    public void doMiningActivityFilterStyle(
            HttpServletResponse response,
            @RequestParam(required = true, value = "serviceUrl", defaultValue = "") String serviceUrl,
            @RequestParam(required = false, value = "mineName", defaultValue = "") String mineName,
            @RequestParam(required = false, value = "startDate", defaultValue = "") String startDate,
            @RequestParam(required = false, value = "endDate", defaultValue = "") String endDate,
            @RequestParam(required = false, value = "oreProcessed", defaultValue = "") String oreProcessed,
            @RequestParam(required = false, value = "producedMaterial", defaultValue = "") String producedMaterial,
            @RequestParam(required = false, value = "cutOffGrade", defaultValue = "") String cutOffGrade,
            @RequestParam(required = false, value = "production", defaultValue = "") String production,
            @RequestParam(required = false, value = "bbox", defaultValue = "") String bboxJson,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {
        //FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(URLDecoder.decode(bboxJson,"UTF-8"));
        FilterBoundingBox bbox = null;
        // Get the mining activities
        // VT: Currently not working as GeoServer is returning strange error for this filer
        String filter = this.mineralOccurrenceService.getMiningActivityFilter(
                mineName, startDate, endDate, oreProcessed, producedMaterial,
                cutOffGrade, production, maxFeatures, bbox);

        String style = this.getStyle(serviceUrl, filter, "er:MiningFeatureOccurrence", "#FF9900");

        response.setContentType("text/xml");

        ByteArrayInputStream styleStream = new ByteArrayInputStream(
                style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }

    /**
     * Handles getting the style of the Earth Resource Mine filter queries. (If the bbox elements are specified, they will limit the output response to 200
     * records implicitly)
     *
     * @param mineName
     *            the name of the mine to query for
     * @param bbox
     * @param maxFeatures
     * @throws Exception
     */
    @RequestMapping("/doMineFilterStyle.do")
    public void doMineFilterStyle(
            HttpServletResponse response,
            @RequestParam(required = true, value = "serviceUrl", defaultValue = "") String serviceUrl,
            @RequestParam(required = false, value = "mineName", defaultValue = "") String mineName,
            @RequestParam(required = false, value = "bbox", defaultValue = "") String bboxJson,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {
        //FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(URLDecoder.decode(bboxJson,"UTF-8"));
        FilterBoundingBox bbox = null;
        // Get the mining activities
        String filter = this.mineralOccurrenceService.getMineFilter(mineName,
                bbox,optionalFilters);

        String style = this.getStyle(serviceUrl, filter, "er:MiningFeatureOccurrence", "#AA0078");

        response.setContentType("text/xml");

        ByteArrayInputStream styleStream = new ByteArrayInputStream(
                style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }
    
    /**
     * Handles getting the style of the Earth Resource Lite Mine View filter queries. (If the bbox elements are specified, they will limit the output response to 200
     * records implicitly)
     *
     * @param optionalFilters
     * @param maxFeatures
     * @throws Exception
     */
    @RequestMapping("/getErlMineViewStyle.do")
    public void getErlMineViewStyle(
            HttpServletResponse response,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {
        //FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(URLDecoder.decode(bboxJson,"UTF-8"));
        FilterBoundingBox bbox = null;
        // Get the mining activities
        GenericFilterAdapter filterObject = new GenericFilterAdapter(optionalFilters,"shape"); 
        String filter = filterObject.getFilterStringAllRecords();

        String style = this.getErLStyle(filter, "erl:MineView", "#a51f2f");

        response.setContentType("text/xml");

        ByteArrayInputStream styleStream = new ByteArrayInputStream(
                style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }
    
    /**
     * Handles getting the style of the Earth Resource Lite Mineral Occurrence filter queries. (If the bbox elements are specified, they will limit the output response to 200
     * records implicitly)
     *
     * @param optionalFilters 
     *            
     * @param maxFeatures
     * @throws Exception
     */
    @RequestMapping("/getErlMineralOccurrenceViewStyle.do")
    public void getErlMineralOccurrenceViewStyle(
            HttpServletResponse response,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {
        //FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(URLDecoder.decode(bboxJson,"UTF-8"));
        FilterBoundingBox bbox = null;
        // Get the mining activities
        GenericFilterAdapter filterObject = new GenericFilterAdapter(optionalFilters,"shape"); 
        String filter = filterObject.getFilterStringAllRecords();

        String style = this.getErLStyle(filter, "erl:MineralOccurrenceView", "#e02e16");

        response.setContentType("text/xml");

        ByteArrayInputStream styleStream = new ByteArrayInputStream(
                style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }
    
    /**
     * Handles getting the style of the Earth Resource Lite Commodity Resource filter queries. (If the bbox elements are specified, they will limit the output response to 200
     * records implicitly)
     *
     * @param optionalFilters 
     *            
     * @param maxFeatures
     * @throws Exception
     */
    @RequestMapping("/getErlCommodityResourceViewStyle.do")
    public void getErlCommodityResourceViewStyle(
            HttpServletResponse response,
            @RequestParam(required = false, value = "optionalFilters") String optionalFilters,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {
        //FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(URLDecoder.decode(bboxJson,"UTF-8"));
        FilterBoundingBox bbox = null;
        // Get the mining activities
        GenericFilterAdapter filterObject = new GenericFilterAdapter(optionalFilters,"shape"); 
        String filter = filterObject.getFilterStringAllRecords();

        String style = this.getErLStyle(filter, "erl:CommodityResourceView", "#940ea3");

        response.setContentType("text/xml");

        ByteArrayInputStream styleStream = new ByteArrayInputStream(
                style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }
                    
                    

    /**
     * Handles counting the results of an Earth Resource Mineral Occurrence style request query.
     *
     * @param commodityName
     * @param bbox
     * @param maxFeatures
     *
     * @throws Exception
     */
    @RequestMapping("/doMineralOccurrenceFilterStyle.do")
    public void doMineralOccurrenceFilterStyle(
            HttpServletResponse response,
            @RequestParam(required = true, value = "serviceUrl", defaultValue = "") String serviceUrl,
            @RequestParam(value = "commodityName", required = false) String commodityName,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, defaultValue="", value = "optionalFilters") String optionalFilters,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {
        //FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(URLDecoder.decode(bboxJson,"UTF-8"));
        FilterBoundingBox bbox = null;
        // Get the mining activities
        String unescapeCommodityName = "";
        if (commodityName != null) {
            unescapeCommodityName = URLDecoder.decode(commodityName, "UTF-8");
        }
        String filter = this.mineralOccurrenceService.getMineralOccurrenceFilter(unescapeCommodityName,
                bbox,optionalFilters);

        String style = this.getStyle(serviceUrl, filter, "gsml:MappedFeature", "#8C489F");

        response.setContentType("text/xml");

        ByteArrayInputStream styleStream = new ByteArrayInputStream(
                style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }

    /**
     * Handles counting the results of an Earth Resource Mineral Occurrence SF0 view style request query.
     *
     * @param commodityName
     * @param bbox
     * @param maxFeatures
     *
     * @throws Exception
     */
    @RequestMapping("/doMinOccurViewFilterStyle.do")
    public void doMinOccurViewFilterStyle(
            HttpServletResponse response,
            @RequestParam(required = true, value = "serviceUrl", defaultValue = "") String serviceUrl,
            @RequestParam(value = "commodityName", required = false) String commodityName,
            @RequestParam(required = false, value = "size") String size,
            @RequestParam(required = false, value = "minOreAmount") String minOreAmount,
            @RequestParam(required = false, value = "minReserves") String minReserves,
            @RequestParam(required = false, value = "minResources") String minResources,
            @RequestParam(required = false, value = "bbox") String bboxJson,
            @RequestParam(required = false, value = "maxFeatures", defaultValue = "0") int maxFeatures)
                    throws Exception {
        //FilterBoundingBox bbox = FilterBoundingBox.attemptParseFromJSON(URLDecoder.decode(bboxJson,"UTF-8"));
        FilterBoundingBox bbox = null;
        // Get the mining activities
        String unescapeCommodityName = "";
        if (commodityName != null) {
            unescapeCommodityName = URLDecoder.decode(commodityName, "UTF-8");
        }
        String filter = this.mineralOccurrenceService.getMinOccurViewFilter(unescapeCommodityName, minOreAmount,
                minReserves, minResources, bbox);

        String style = this.getStyle(serviceUrl, filter, EarthResourcesDownloadController.MIN_OCCUR_VIEW_TYPE, "#ed9c38");

        response.setContentType("text/xml");

        ByteArrayInputStream styleStream = new ByteArrayInputStream(
                style.getBytes());
        OutputStream outputStream = response.getOutputStream();

        FileIOUtil.writeInputToOutputStream(styleStream, outputStream, 1024, false);

        styleStream.close();
        outputStream.close();
    }
    
    public String getErLStyle(String filter, String name, String color) throws IOException{
    	 Hashtable<String,String> valueMap = new Hashtable<String,String>();
         valueMap.put("filter", filter);
         valueMap.put("name", name);
         valueMap.put("color", color);

         return  SLDLoader.loadSLD("/org/auscope/portal/slds/erl_MineView.sld", valueMap,false);
    }

    public String getStyle(String serviceUrl, String filter, String name, String color) {
        //VT : This is a hack to get around using functions in feature chaining
        // https://jira.csiro.au/browse/SISS-1374
        // there are currently no available fix as wms request are made prior to
        // knowing app-schema mapping.

        String style = "<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n"
                + "<StyledLayerDescriptor version=\"1.0.0\" xmlns:mo=\"http://xmlns.geoscience.gov.au/minoccml/1.0\" "
                + getERMLNamespaces(serviceUrl) 
                + "xsi:schemaLocation=\"http://www.opengis.net/sld StyledLayerDescriptor.xsd\" xmlns:ogc=\"http://www.opengis.net/ogc\" xmlns:xlink=\"http://www.w3.org/1999/xlink\" xmlns:sld=\"http://www.opengis.net/sld\" xmlns:xsi=\"http://www.w3.org/2001/XMLSchema-instance\">"
                + "<NamedLayer>" + "<Name>"
                + name + "</Name>"
                + "<UserStyle>"
                + "<Title>" + name + "</Title>"
                + "<FeatureTypeStyle>"
                + "<Rule>"
                + filter
                + "<PointSymbolizer>"
                + "<Graphic>"
                + "<Mark>"
                + "<WellKnownName>circle</WellKnownName>"
                + "<Fill>"
                + "<CssParameter name=\"fill\">" + color + "</CssParameter>"
                + "<CssParameter name=\"fill-opacity\">0.4</CssParameter>"
                + "</Fill>"
                + "<Stroke>"
                + "<CssParameter name=\"stroke\">" + color + "</CssParameter>"  
                + "<CssParameter name=\"stroke-width\">1</CssParameter>"
                + "</Stroke>"
                + "</Mark>"
                + "<Size>8</Size>"
                + "</Graphic>"
                + "</PointSymbolizer>"
                + "</Rule>"
                + "</FeatureTypeStyle>"
                + "</UserStyle>" + "</NamedLayer>" + "</StyledLayerDescriptor>";
        return style;
    }
    
    private String getERMLNamespaces(String serviceUrl) {
        String erNamespace;
        String gmlNamespace;
        String gsmlNamespace;
        
        ServiceConfigurationItem config = serviceConfig.getServiceConfigurationItem(serviceUrl);
        if (config != null && config.isGml32()) {
            // use ERML 2.0 namespaces
            erNamespace = "http://xmlns.earthresourceml.org/EarthResource/2.0";
            gmlNamespace = "http://www.opengis.net/gml/3.2";
            gsmlNamespace = "http://xmlns.geosciml.org/GeoSciML-Core/3.2";
        } else {
            // use ERML 1.1 namespaces
            erNamespace = "urn:cgi:xmlns:GGIC:EarthResource:1.1";
            gmlNamespace = "http://www.opengis.net/gml";
            gsmlNamespace = "urn:cgi:xmlns:CGI:GeoSciML:2.0";
        }
        StringBuffer sb = new StringBuffer();
        sb.append("xmlns:er=\"").append(erNamespace).append("\" ");
        sb.append("xmlns:gml=\"").append(gmlNamespace).append("\" ");
        sb.append("xmlns:gsml=\"").append(gsmlNamespace).append("\" ");
        return sb.toString();
            
    }
    

}
