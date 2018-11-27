package sign;

import java.io.File;
import java.io.IOException;
import java.io.StringWriter;
import java.util.Base64;

import javax.swing.JFileChooser;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.soap.SOAPException;
import javax.xml.soap.SOAPMessage;
import javax.xml.transform.OutputKeys;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;

import org.w3c.dom.DOMException;
import org.w3c.dom.Document;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import savingFile.FileSaver;
import soap.SOAPClient;

public class TimeStamp {
	public static void addTimeStamp()
			throws SAXException, IOException, ParserConfigurationException, DOMException, SOAPException {
		File signedFile = getFile();

		DocumentBuilderFactory dbFactory = DocumentBuilderFactory.newInstance();
		DocumentBuilder dBuilder = dbFactory.newDocumentBuilder();
		Document doc = dBuilder.parse(signedFile);

		NodeList nList = doc.getElementsByTagName("ds:SignatureValue");
		String signedValue = null;

		if (nList.getLength() == 1) {
			Node nNode = nList.item(0);

			if (nNode.getNodeType() == Node.ELEMENT_NODE) {

				signedValue = nNode.getTextContent();
				System.out.println("Selected signed value: " + signedValue);
			}

			String signedValue64 = Base64.getEncoder().encodeToString(signedValue.getBytes("utf-8"));
			System.out.println(signedValue64);
			SOAPMessage response = SOAPClient.getTimestamp(signedValue64);

			nNode = doc.getElementsByTagName("xades:QualifyingProperties").item(0);

			Node newNode = nNode.appendChild(doc.createElement("UnSignedProperties"));
			newNode = newNode.appendChild(doc.createElement("UnsignedSignatureProperties"));
			newNode = newNode.appendChild(doc.createElement("SignatureTimeStamp"));
			newNode = newNode.appendChild(doc.createElement("EncapsulatedTimeStamp")); // base64 value, optionalatributy
																						// (Id, Encoding)
			newNode.appendChild(doc.createTextNode(response.getSOAPBody().getTextContent()));

//			nNode = doc.getElementsByTagName("SignatureTimeStamp").item(0);
//			newNode = newNode.appendChild(doc.createElement("XMLTimeStamp"));
			
			StringWriter outputWriter = new StringWriter();
			outputWriter.write(xmlToString(doc));

			FileSaver.saveFile(outputWriter, new String("xml"));
		}
	}

	public static String xmlToString(Document doc) {
	    try {
	        StringWriter sw = new StringWriter();
	        TransformerFactory tf = TransformerFactory.newInstance();
	        Transformer transformer = tf.newTransformer();
	        transformer.setOutputProperty(OutputKeys.OMIT_XML_DECLARATION, "no");
	        transformer.setOutputProperty(OutputKeys.METHOD, "xml");
	        transformer.setOutputProperty(OutputKeys.INDENT, "yes");
	        transformer.setOutputProperty(OutputKeys.ENCODING, "UTF-8");

	        transformer.transform(new DOMSource(doc), new StreamResult(sw));
	        return sw.toString();
	    } catch (Exception ex) {
	        throw new RuntimeException("Error converting to String", ex);
	    }
	}

	public static File getFile() {
		JFileChooser fileChooser = new JFileChooser();
		fileChooser.setCurrentDirectory(new File(System.getProperty("user.home")));
		int result = fileChooser.showOpenDialog(null);
		if (result == JFileChooser.APPROVE_OPTION) {
			System.out.println("Selected file: " + fileChooser.getSelectedFile().getAbsolutePath());
			return fileChooser.getSelectedFile();
		}
		return null;
	}

}
