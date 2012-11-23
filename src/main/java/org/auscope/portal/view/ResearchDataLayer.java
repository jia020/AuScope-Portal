package org.auscope.portal.view;

import org.auscope.portal.core.view.knownlayer.KnownLayer;
import org.auscope.portal.core.view.knownlayer.KnownLayerSelector;

/**
 * This class extends KnownLayer to provide a specialisation for research data.
 * It enables us to render such data separately from KnownLayer by serving as
 * a discriminator.
 * @author bro879
 *
 */
public class ResearchDataLayer extends KnownLayer {

    /** auto generated version ID */
    private static final long serialVersionUID = -5216913031450346817L;

    /**
     * Initialises a new instance of the ResearchDataLayer class.
     * Defers to the super constructor.
     * @param id the unique ID for this layer (if null it will be autogenerated using an internal static counter).
     * @param knownLayerSelector how this known layer will select CSW records.
     */
    public ResearchDataLayer(String id, KnownLayerSelector knownLayerSelector) {
        super(id, knownLayerSelector);
    }
}