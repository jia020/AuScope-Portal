package org.auscope.portal.server.web.controllers;

import org.auscope.portal.csw.CSWGetDataRecordsFilter;
import org.auscope.portal.csw.CSWRecord;
import org.auscope.portal.server.domain.filter.FilterBoundingBox;
import org.auscope.portal.server.web.service.CSWFilterService;
import org.auscope.portal.server.web.view.ViewCSWRecordFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.servlet.ModelAndView;

/**
 * A controller class for marshalling access to the underling CSWFilterService
 * @author Josh Vote
 */
public class CSWFilterController extends BaseCSWController {
    private static final int DEFAULT_MAX_RECORDS = 100;
    private CSWFilterService cswFilterService;

    /**
     * Creates a new CSWFilterController with the specified dependencies.
     * @param cswFilterService Used to make filtered CSW requests
     * @param viewCSWRecordFactory Used to transform CSWRecords for the view
     */
    @Autowired
    public CSWFilterController(CSWFilterService cswFilterService,
            ViewCSWRecordFactory viewCSWRecordFactory) {
        super(viewCSWRecordFactory);
        this.cswFilterService = cswFilterService;
        this.viewCSWRecordFactory = viewCSWRecordFactory;
    }

    /**
     * Attempts to parse a FilterBoundingBox from the given coords (if they exist). Returns null on failure
     * @return
     */
    private FilterBoundingBox attemptParseBBox(Double westBoundLongitude, Double eastBoundLongitude,
            Double northBoundLatitude, Double southBoundLatitude) {
        FilterBoundingBox filterBbox = null;
        if (westBoundLongitude != null && eastBoundLongitude != null &&
                northBoundLatitude != null && southBoundLatitude != null) {
            filterBbox = new FilterBoundingBox("",
                        new double[] {eastBoundLongitude, southBoundLatitude},
                        new double[] {westBoundLongitude, northBoundLatitude});
        }

        return filterBbox;
    }

    /**
     * Gets a list of CSWRecord view objects filtered by the specified values from all internal
     * CSW's
     * @param westBoundLongitude [Optional] Spatial bbox constraint
     * @param eastBoundLongitude [Optional] Spatial bbox constraint
     * @param northBoundLatitude [Optional] Spatial bbox constraint
     * @param southBoundLatitude [Optional] Spatial bbox constraint
     * @param keywords [Optional] One or more keywords to filter by
     * @param capturePlatform [Optional]  A capture platform filter
     * @param sensor [Optional] A sensor filter
     * @return
     */
    @RequestMapping("/getFilteredCSWRecords.do")
    public ModelAndView getFilteredCSWRecords(
            @RequestParam(value="westBoundLongitude", required=false) Double westBoundLongitude,
            @RequestParam(value="eastBoundLongitude", required=false) Double eastBoundLongitude,
            @RequestParam(value="northBoundLatitude", required=false) Double northBoundLatitude,
            @RequestParam(value="southBoundLatitude", required=false) Double southBoundLatitude,
            @RequestParam(value="keyword", required=false) String[] keywords,
            @RequestParam(value="capturePlatform", required=false) String capturePlatform,
            @RequestParam(value="sensor", required=false) String sensor) {

        //Firstly generate our filter
        FilterBoundingBox filterBbox = attemptParseBBox(westBoundLongitude, eastBoundLongitude,
                northBoundLatitude, southBoundLatitude);
        CSWGetDataRecordsFilter filter = new CSWGetDataRecordsFilter(filterBbox, keywords, capturePlatform, sensor);
        log.debug(String.format("filter '%1$s'", filter));

        //Then make our requests to all of CSW's
        CSWRecord[] records = null;
        try {
            records = cswFilterService.getFilteredRecords(filter, DEFAULT_MAX_RECORDS);
        } catch (Exception ex) {
            log.warn(String.format("Error fetching filtered records for filter '%1$s'", filter), ex);
        }

        return generateJSONResponseMAV(records);
    }

    /**
     * Gets a list of CSWRecord view objects filtered by the specified values from all internal
     * CSW's
     * @param westBoundLongitude [Optional] Spatial bbox constraint
     * @param eastBoundLongitude [Optional] Spatial bbox constraint
     * @param northBoundLatitude [Optional] Spatial bbox constraint
     * @param southBoundLatitude [Optional] Spatial bbox constraint
     * @param keywords [Optional] One or more keywords to filter by
     * @param capturePlatform [Optional]  A capture platform filter
     * @param sensor [Optional] A sensor filter
     * @return
     */
    @RequestMapping("/getFilteredCSWRecordsCount.do")
    public ModelAndView getFilteredCSWRecordsCount(
            @RequestParam(value="westBoundLongitude", required=false) Double westBoundLongitude,
            @RequestParam(value="eastBoundLongitude", required=false) Double eastBoundLongitude,
            @RequestParam(value="northBoundLatitude", required=false) Double northBoundLatitude,
            @RequestParam(value="southBoundLatitude", required=false) Double southBoundLatitude,
            @RequestParam(value="keyword", required=false) String[] keywords,
            @RequestParam(value="capturePlatform", required=false) String capturePlatform,
            @RequestParam(value="sensor", required=false) String sensor) {

        //Firstly generate our filter
        FilterBoundingBox filterBbox = attemptParseBBox(westBoundLongitude, eastBoundLongitude,
                northBoundLatitude, southBoundLatitude);
        CSWGetDataRecordsFilter filter = new CSWGetDataRecordsFilter(filterBbox, keywords, capturePlatform, sensor);
        log.debug(String.format("filter '%1$s'", filter));

        //Then make our requests to all of CSW's
        int count = 0;
        try {
            count = cswFilterService.getFilteredRecordsCount(filter, DEFAULT_MAX_RECORDS);
        } catch (Exception ex) {
            log.warn(String.format("Error fetching filtered record count for filter '%1$s'", filter), ex);
            return generateJSONResponseMAV(false, null, "Error fetching filtered record count");
        }

        return generateJSONResponseMAV(true, count, "");
    }
}
