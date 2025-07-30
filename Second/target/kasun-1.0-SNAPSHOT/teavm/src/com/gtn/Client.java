package com.gtn;

import org.teavm.jso.JSBody;
import org.teavm.jso.JSFunctor;
import org.teavm.jso.JSObject;
import org.teavm.jso.JSProperty;
import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.events.EventListener;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLInputElement;
import org.teavm.jso.file.File;
import org.teavm.jso.function.JSConsumer;


public class Client {


    public static class Person {
        private String name;
        private int age;
        private String email;

        public Person(String name, int age, String email) {
            this.name = name;
            this.age = age;
            this.email = email;
        }

        public String getName() {
            return name;
        }

        public int getAge() {
            return age;
        }

        public String getEmail() {
            return email;
        }

        @Override
        public String toString() {
            return "Person{" +
                    "name='" + name + '\'' +
                    ", age=" + age +
                    ", email='" + email + '\'' +
                    '}';
        }
    }


    public static void main(String[] args) {


        var document = HTMLDocument.current();


        //create main container
        var container = document.createElement("div");
        container.getStyle().setProperty("padding", "20px");
        container.getStyle().setProperty("font-family", "Arial", "sans-serif");

        //title
        var title = document.createElement("h2");
        title.setTextContent("File Processor");
        container.appendChild(title);

        //create instructions
        var instructions = document.createElement("p");
        instructions.setTextContent("Upload a text file with data in format: Name, Age, Email (one per line)");
        container.appendChild(instructions);

        //create file input
        var fileInput = (HTMLInputElement) document.createElement("input");
        fileInput.setType("file");
        fileInput.setAttribute("accept", ".txt");
        fileInput.getStyle().setProperty("margin", "10px 0");
        fileInput.getStyle().setProperty("padding", "10px");
        fileInput.getStyle().setProperty("border", "2px dashed #ccc");


        //create output area
        var outputArea = document.createElement("div");
        outputArea.setId("output");
        outputArea.getStyle().setProperty("margin-top", "20px");
        outputArea.getStyle().setProperty("padding", "15px");
        outputArea.getStyle().setProperty("border", "1px solid #ddd");
        outputArea.getStyle().setProperty("background-color", "#f9f9f9");
        outputArea.getStyle().setProperty("min-height", "100px");
        outputArea.setTextContent("Upload a file to see result here");


//        event listener to file input
        fileInput.addEventListener("change", new EventListener<Event>() {
            @Override
            public void handleEvent(Event event) {
                if (fileInput.getFiles().getLength() > 0) {
                    readFile(fileInput.getFiles().item(0), outputArea);
                }
            }
        });


        //add all elements to container
        container.appendChild(fileInput);
        container.appendChild(outputArea);

        //add container to body
        document.getBody().appendChild(container);


    }


    private static void readFile( File file, HTMLElement outputArea) {
        outputArea.setTextContent("Reading file....");

        // Use callback approach instead of direct JavaMethod invocation
        readFileContent(file, content -> {
            if (content != null) {
                processFileContent(content, outputArea);
            } else {
                outputArea.setTextContent("Error reading file");
            }
        });
    }




    // JavaScript bridge to read file content - Using callback approach
    @JSBody(params = {"file","callback"}, script = """
        const reader = new FileReader();
        reader.onload = function(e) {
            var content = e.target.result;
            console.log("File content loaded successfully");
            callback(content);
        };
        reader.onerror = function(e) {
            console.error("Error reading file:", e.target.error);
            callback(null);
        };
        reader.readAsText(file);
    """)
    private static native void readFileContent(File file, JSContentCallback callback);

    // Callback interface for file content
    @JSFunctor
    public interface JSContentCallback extends JSObject {
        void onContent(String content);
    }



    public static void processFileContent(String content, HTMLElement outputArea) {
        HTMLDocument document = HTMLDocument.current();

        //clear
        outputArea.setInnerHTML("");

        //create header
        var header = document.createElement("h3");
        header.setTextContent("Parsed Objects:");
        header.getStyle().setProperty("color", "#333");
        outputArea.appendChild(header);


        //split content in to lines
        String[] lines = content.split("\\r?\\n");
        int successCount = 0;
        int errorCount = 0;

        var resultsContainer = document.createElement("div");

        for (String line : lines) {
            line = line.trim();
            if (!line.isEmpty()) {
                try {

                    // Parse line: Name,Age,Email
                    String[] parts = line.split(",");
                    if (parts.length >= 3) {
                        // Create Person object
                        Person person = new Person(
                                parts[0].trim(),
                                Integer.parseInt(parts[1].trim()),
                                parts[2].trim()
                        );

                        // Create display element for this person
                        var personDiv = document.createElement("div");
                        personDiv.getStyle().setProperty("background-color", "#e8f5e8");
                        personDiv.getStyle().setProperty("margin", "5px 0");
                        personDiv.getStyle().setProperty("padding", "10px");
                        personDiv.getStyle().setProperty("border-radius", "4px");
                        personDiv.getStyle().setProperty("border-left", "4px solid #4CAF50");
                        personDiv.setTextContent("✓ " + person.toString());

                        resultsContainer.appendChild(personDiv);
                        successCount++;

//

                    } else {
                        throw new Exception("Invalid format - expected 3 parts separated by commas");
                    }


                } catch (Exception e) {

                    // Create error display element
                    var errorDiv = document.createElement("div");
                    errorDiv.getStyle().setProperty("background-color", "#ffe8e8");
                    errorDiv.getStyle().setProperty("margin", "5px 0");
                    errorDiv.getStyle().setProperty("padding", "10px");
                    errorDiv.getStyle().setProperty("border-radius", "4px");
                    errorDiv.getStyle().setProperty("border-left", "4px solid #f44336");
                    errorDiv.setTextContent("✗ Error parsing: \"" + line + "\" - " + e.getMessage());

                    resultsContainer.appendChild(errorDiv);
                    errorCount++;

                }
            }
        }


        var summary = document.createElement("p");
        summary.getStyle().setProperty("font-weight", "bold");
        summary.getStyle().setProperty("margin-top", "15px");
        summary.setTextContent("Summary: " + successCount + " objects created, " + errorCount + " errors");

        outputArea.appendChild(resultsContainer);
        outputArea.appendChild(summary);


    }


}