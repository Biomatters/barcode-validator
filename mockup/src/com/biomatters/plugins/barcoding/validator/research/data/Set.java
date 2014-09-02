package com.biomatters.plugins.barcoding.validator.research.data;

import com.biomatters.geneious.publicapi.documents.MalformedURNException;
import com.biomatters.geneious.publicapi.documents.URN;
import com.biomatters.geneious.publicapi.documents.XMLSerializable;
import com.biomatters.geneious.publicapi.documents.XMLSerializationException;
import org.jdom.Element;

import java.util.ArrayList;
import java.util.List;

/**
 * Set of input to the barcode validation pipeline.  Consists of a barcode sequence
 *
 * @author Matthew Cheung
 *         Created on 2/09/14 11:26 AM
 */
public class Set implements XMLSerializable {

    URN barcodeSeqUrn;
    List<URN> traceUrns;


    public Set(URN barcodeSeqUrn, List<URN> traceUrns) {
        this.barcodeSeqUrn = barcodeSeqUrn;
        this.traceUrns = traceUrns;
    }

    public Set(Element element) throws XMLSerializationException {
        try {
            barcodeSeqUrn = URN.fromXML(element.getChild(BARCODE));
            traceUrns = new ArrayList<URN>();
            for (Element traceElement : element.getChildren(TRACE)) {
                traceUrns.add(URN.fromXML(traceElement));
            }
        } catch (MalformedURNException e) {
            throw new XMLSerializationException(e.getMessage(), e);
        }
    }

    private static final String BARCODE = "barcode";
    private static final String TRACE = "trace";

    @Override
    public Element toXML() {
        Element root = new Element(ROOT_ELEMENT_NAME);
        root.addContent(barcodeSeqUrn.toXML(BARCODE));
        for (URN traceUrn : traceUrns) {
            root.addContent(traceUrn.toXML(TRACE));
        }
        return root;
    }

    @Override
    public void fromXML(Element element) throws XMLSerializationException {
        throw new UnsupportedOperationException("Use constructor instead.");
    }

    public List<String> getUrnStringList() {
        List<String> list = new ArrayList<String>();
        list.add(barcodeSeqUrn.toString());
        for (URN traceUrn : traceUrns) {
            list.add(traceUrn.toString());
        }
        return list;
    }
}
