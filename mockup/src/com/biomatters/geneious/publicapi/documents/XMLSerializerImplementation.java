package com.biomatters.geneious.publicapi.documents;

import com.biomatters.geneious.publicapi.plugin.Geneious;
import jebl.util.ProgressListener;
import org.jdom.Element;

import java.lang.reflect.Constructor;
import java.lang.reflect.InvocationTargetException;
import java.util.Map;

/**
 * @author Matthew Cheung
 *         Created on 18/08/14 9:23 AM
 */
public class XMLSerializerImplementation implements XMLSerializer.ClassSerializer {

    public static void setup() {
        XMLSerializer.setClassSerializer(new XMLSerializerImplementation());
    }

    @Override
    public Element classToXML(Geneious.MajorVersion version, String elementName, XMLSerializable object, ProgressListener progress) {
        Element root = object.toXML();
        root.setName(elementName);
        return root;
    }

    @Override
    public <T extends XMLSerializable> T classFromXML(Element element, Class<? extends T> cl, ProgressListener progress) throws XMLSerializationException {
        try {
            Constructor<? extends T> constructor = cl.getConstructor(Element.class);
            return constructor.newInstance(element);
        } catch (NoSuchMethodException e) {
            throw new XMLSerializationException(e.getMessage(), e);
        } catch (InstantiationException e) {
            throw new XMLSerializationException(e.getMessage(), e);
        } catch (IllegalAccessException e) {
            throw new XMLSerializationException(e.getMessage(), e);
        } catch (InvocationTargetException e) {
            throw new XMLSerializationException(e.getMessage(), e);
        }
    }

    @Override
    public Element fieldValueToElement(Geneious.MajorVersion version, String elementName, Object value) {
        return null;
    }

    @Override
    public Element fieldValuesToElement(Geneious.MajorVersion version, String name, Map<String, Object> fieldValues, boolean silentlyExcludeXmlSerializableFieldsThatDontSupportThisVersion) {
        return null;
    }

    @Override
    public Object getFieldValue(Element element) {
        return null;
    }

    @Override
    public void fieldsFromElement(Element element, Map<String, Object> fieldValues) {

    }

    @Override
    public Class forName(String className) throws ClassNotFoundException {
        return Class.forName(className);
    }

    @Override
    public Element pluginDocumentToXml(Geneious.MajorVersion version, PluginDocument pluginDocument, ProgressListener progressListener) throws XMLSerializationException {
        return classToXML(version, XMLSerializable.ROOT_ELEMENT_NAME, pluginDocument, progressListener);
    }

    @Override
    public Geneious.MajorVersion getFieldValueToElementOldVersionSupport(XMLSerializable.VersionSupportType versionSupportType, Object... values) {
        return Geneious.getMajorVersion();
    }

    @Override
    public void translateUrns(Element element, Map<URN, URN> urnChanges) {

    }
}
