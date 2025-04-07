import React, { useState } from "react";
import {Link} from "react-router-dom";
import "../../index.css";
import logo from "../../assets/logo.png"
import gif from "../../assets/demo_fig.gif"

function Home() {
    return (
        <>
            <div className="title-container">
            <div className="title">
                AIM(s): Artificial Intelligence Monitoring for Surgery
            </div>

            <div className="subheading">
                A full system solution to track surgical disposable usage in the operating room.
            </div>
            </div>
            
            <div class="wave">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 20 1440 120"><path fill="#000000" fill-opacity="1" d="M0,32L60,32C120,32,240,32,360,48C480,64,600,96,720,96C840,96,960,64,1080,48C1200,32,1320,32,1380,32L1440,32L1440,0L1380,0C1320,0,1200,0,1080,0C960,0,840,0,720,0C600,0,480,0,360,0C240,0,120,0,60,0L0,0Z"></path></svg>
            </div>

            <div className="home-container">
            <div className="textbox">

            <div className="header">What is AIM(s)?</div>

            <div className="body">
                The Spring 2025 AIM(s) project is a culmination of research, proof of concept, and end-to-end system development. Starting from scratch from a simple pitch, this team went through several phases of development that ended with a complete piece of software that is in the process of being used during real-world surgeries at Orlando Health. Each stage proved detailed research and solutions that have been expanded upon as the final product came together. With this site detailing each stage of development, we welcome you to the world of real-time AI-powered object detection, classification, tracking, and recording. 
            </div>
            </div>

            <div className="imgbox">
                <img className="image" src={gif} alt="AIMs gif" />
            </div>
            </div>

            <div className="fullwidth-section">
            <div className="header2">Project Problem</div>
            <div className="body">
                Prior to surgery, the necessary equipment for that operation must be laid out for the surgical team. These tool types and amounts are then indicated in a predetermined checklist. The staple loads are one-use only; once opened, they must be used or thrown away as they are now unsterile. Since there is a standardized amount to be opened, issues arise where not all the loads are used. Additionally, workers are less likely to report their mistakes, leading to concerns with human error.
            </div>

            <div className="buffer-small"></div>

            <div className="header2">Proposed Solution</div>
            <div className="body">
                AIM(s) is a full system solution that can be integrated into the operating room, utilizing AI to track in real time the number of loads that are used throughout a surgery. The results of such are logged throughout the operation and output to a report for further analysis and post-processing. At approximately $250 per load with 1200 surgeries annually, this tool can quickly save thousands of dollars for the hospital and reduce unnecessary waste in surgery.
            </div>
            </div>

            <div className="buffer"></div>

            <div className="thanks">
                <div className="ty-header">
                    Special Thanks
                </div>
                <div className="ty-body">
                    <div>Dr. Laura Brattain</div>
                    <div>Dr. Alexis Sanchez</div>
                    <div>Dr. Lillian Aguirre</div>
                    <div>All of the Orlando Health staff that helped us to test our software throughout demoing.</div>
                </div>
            </div>

            <div className="buffer"></div>
        </>
    )
}

export default Home;