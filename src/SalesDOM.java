import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;

import javax.xml.XMLConstants;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.Transformer;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.dom.DOMSource;
import javax.xml.transform.stream.StreamResult;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Scanner;
import java.util.logging.Logger;

public class SalesDOM {
    static final String CLASS_NAME = SalesDOM.class.getSimpleName();
    static final Logger LOG = Logger.getLogger(CLASS_NAME);

    public static void main(String argv[]) {
        if (argv.length != 1) {
            LOG.severe("Falta archivo XML como argumento.");
            System.exit(1);
        }

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();

        try {
            dbf.setFeature(XMLConstants.FEATURE_SECURE_PROCESSING, true);

            DocumentBuilder db = dbf.newDocumentBuilder();

            Document doc = db.parse(new File(argv[0]));

            doc.getDocumentElement().normalize();

            double ventasTotales = salesTotal(doc);

            //System.out.printf("\nVentas totales: $%,8.2f \n", ventasTotales);

            //salesState(doc, "Arizona");

            //reporteVentas(doc);

            double porcentaje = obtenerPorcentaje();

            String departamento = obtenerDepartamento();


            nuevoValor(doc, departamento, porcentaje);

            nuevoDocumento(doc, "new_sales.xml");

        } catch (ParserConfigurationException e) {
            LOG.severe(e.getMessage());
        } catch (IOException e) {
            LOG.severe(e.getMessage());
        } catch (SAXException e) {
            LOG.severe(e.getMessage());
        }
    }

    private static double obtenerPorcentaje() {
        double porcentaje;
        Scanner scanner = new Scanner(System.in);
        do {
            System.out.print("Ingrese un porcentaje entre 5% y 15%: \n" );
            porcentaje = scanner.nextDouble();
        } while (porcentaje < 5 || porcentaje > 15);
        return porcentaje;
    }
    private static String obtenerDepartamento() {
        Scanner scanner = new Scanner(System.in);
        System.out.print("Ingrese el departamento: ");
        return scanner.nextLine();
    }
    private static void nuevoValor(Document doc, String departamento, double porcentaje) {
        Element root = doc.getDocumentElement();

        NodeList salesData = root.getElementsByTagName("sale_record");

        for (int index = 0; index < salesData.getLength(); index++) {
            Node node = salesData.item(index);

        if (node.getNodeType() == Node.ELEMENT_NODE) {
        Element element = (Element) node;
        String departmento = element.getElementsByTagName("department").item(0).getTextContent();

        if (departamento.equalsIgnoreCase(departamento)) {
        double ventasOriginales = Double.parseDouble(element.getElementsByTagName("sales").item(0).getTextContent());
        double ventasActualizadas = ventasOriginales + (ventasOriginales * porcentaje / 100);
        element.getElementsByTagName("sales").item(0).setTextContent(String.format("%.2f", ventasActualizadas));

        }
        }
     }
    }

    private static void nuevoDocumento(Document doc, String s) {
        try (FileWriter writer = new FileWriter("new_sales.xml")) {
            TransformerFactory transformerFactory = TransformerFactory.newInstance();
            Transformer transformer = transformerFactory.newTransformer();
            transformer.transform(new DOMSource(doc), new StreamResult(writer));

            System.out.println("Se ha creado el nuevo archivo new_sales.xml");
        } catch (Exception e) {
            System.err.println("Error al guardar el archivo: " + e.getMessage());
        }
    }






    public static double salesTotal(Document doc) {
        double sum = 0.0;

        Element root = doc.getDocumentElement();

        // sacar unicamente las ventas
        NodeList salesData = root.getElementsByTagName("sales");

        for (int i = 0; i < salesData.getLength(); i++) {

            Element salesElement = (Element) salesData.item(i);

            if (salesElement.getNodeType() == Node.ELEMENT_NODE) {
                String tag = salesElement.getNodeName();
                String value = salesElement.getFirstChild().getNodeValue();
               // System.out.printf("\tName: %s Value: %s\n", tag, value);

                sum = sum + Double.parseDouble(value);
            }
        }
        return sum;
    }

    public static void salesState(Document doc, String s) {
        Element root = doc.getDocumentElement();

        NodeList salesData = root.getElementsByTagName("sale_record");

        int n = salesData.getLength();

        for (int index = 0; index < n; index++) {
            Node node = salesData.item(index);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                String id = element.getElementsByTagName("id").item(0).getTextContent();
                String first_name = element.getElementsByTagName("first_name").item(0).getTextContent();
                String last_name = element.getElementsByTagName("last_name").item(0).getTextContent();
                String sales = element.getElementsByTagName("sales").item(0).getTextContent();
                String state = element.getElementsByTagName("state").item(0).getTextContent();
                String department = element.getElementsByTagName("department").item(0).getTextContent();

                if (state.equals(s)) {
                   System.out.printf("%4.4s %10.10s %-10.10s %,7.2f %-10.10s %-15.15s\n",
                            id, first_name, last_name, Double.parseDouble(sales), state, department);
                }
            }
        }
    }

    public static void reporteVentas(Document doc) {
        Element root = doc.getDocumentElement();

        NodeList salesData = root.getElementsByTagName("sale_record");

        int n = salesData.getLength();

        HashMap<String,Double> ventas = new HashMap<>();

       double total = 0.0;

        String sales;
        String department;
        for (int index = 0; index < n; index++) {
            Node node = salesData.item(index);

            if (node.getNodeType() == Node.ELEMENT_NODE) {
                Element element = (Element) node;

                 sales = element.getElementsByTagName("sales").item(0).getTextContent();
                 department = element.getElementsByTagName("department").item(0).getTextContent();

                double val = Double.parseDouble(sales);

                if( ventas.containsKey(department) ) {
                    double x = ventas.get(department);
                    ventas.put(department, val + x );
                } else {
                    ventas.put(department,val);
                }
                total = total + val;
            }

        }

        for (Map.Entry<String , Double> entry: ventas.entrySet()) {
            System.out.printf("%-15.15s %,7.2f  (%2.2f %%) \n", entry.getKey(),
                    entry.getValue(),
                    (entry.getValue()/total) * 100 );
        }


    }
}
