package com.tvmtest;

import org.teavm.jso.dom.events.Event;
import org.teavm.jso.dom.html.HTMLDocument;
import org.teavm.jso.dom.html.HTMLElement;
import org.teavm.jso.dom.html.HTMLInputElement;

public class Client {
    public static void main(String[] args) {
        var document = HTMLDocument.current();

        //create input field
        var input = document.createElement("input");
        input.setAttribute("type","text");
        input.setAttribute("placeholder","Type Something");

        //create button
        var button = document.createElement("button");
        button.appendChild(document.createTextNode("Show"));

        //create output field
        var output = document.createElement("div");
        output.setAttribute("id","output");

        //add Elements to body
        HTMLElement body = document.getBody();
        body.appendChild(input);
        body.appendChild(button);
        body.appendChild(output);

        //add event listener
        button.addEventListener("click",(Event event)->{

            String text = ((HTMLInputElement)input).getValue();
            output.setInnerHTML("You typed "+text);


        });
    }
}
