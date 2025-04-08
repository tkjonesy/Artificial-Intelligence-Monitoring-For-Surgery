import React, { useState } from "react";
import {Link} from "react-router-dom";
import "../../index.css";
import aimlogo from "../../assets/aimlogo.png";

function Other() {
    return (
        <>
            <div className="title-container">
            <div className="title">
                Other Applications
            </div>

            <div className="subheading">
                An adaptive concept to use the software in a versatile and expansive way.
            </div>
            </div>
            
            <div class="wave">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 20 1440 120"><path fill="#000000" fill-opacity="1" d="M0,32L60,32C120,32,240,32,360,48C480,64,600,96,720,96C840,96,960,64,1080,48C1200,32,1320,32,1380,32L1440,32L1440,0L1380,0C1320,0,1200,0,1080,0C960,0,840,0,720,0C600,0,480,0,360,0C240,0,120,0,60,0L0,0Z"></path></svg>
            </div>

            <div className="other-container">
                <div className="textbox">

                <div className="header">
                    Expanding the Use Case
                </div>
                <div className="body">
                    Our project began with the goal to create a technology that was able to perform real-time object detection and classification within one environment. From that point, we were able to expand the project past a research heavy and proof-of-concept software into a robust application that is being integrated into a real-world hospital setting. However, that is not the bounds of its possibilities. 
                </div>

                <div className="header">
                    The AIM Concept
                </div>
                <div className="body">
                    The goal of this software is to be able to apply it to any use case with minor tweaking. To reflect this, we started with the Artificial Intelligence Monitoring (AIM) concept. In tandem with our efforts in implementing this project in an operating room setting, we have created a universal, plug-and-play software. In concept, the user can take any .onnx model with its corresponding .names classes and add them to the application. So far, testing has been implemented with our proof-of-concept custom trained YOLO model as well as the generic COCO trained YOLO model provided. Our team sees this as an opportunity to encourage further development of the software to handle even more use cases.  
                </div>

                <div className="header5">
                    Want to use AIM for your own project?
                </div>
                <div className="body">
                    Please reference our GitHub documentation for further information on how to download the software and adapt it to your own environment.
                </div>
                </div>

                <div className="imgbox">
                    <img className="image" src={aimlogo} alt="AIM logo" />
                </div>
            </div>
            <div className="buffer"></div>
        </>
    )
}

export default Other;