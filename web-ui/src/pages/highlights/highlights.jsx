import React, { useState } from "react";
import {Link} from "react-router-dom";
import "../../index.css";

function Highlights() {
    return (
        <>
            <div className="title-container">
            <div className="title">
                The Highlights
            </div>

            <div className="subheading">
                A overarching look at what the program provides to the user as well as at a technical standpoint.
            </div>
            </div>
            
            <div class="wave">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 20 1440 120"><path fill="#000000" fill-opacity="1" d="M0,32L60,32C120,32,240,32,360,48C480,64,600,96,720,96C840,96,960,64,1080,48C1200,32,1320,32,1380,32L1440,32L1440,0L1380,0C1320,0,1200,0,1080,0C960,0,840,0,720,0C600,0,480,0,360,0C240,0,120,0,60,0L0,0Z"></path></svg>
            </div>

            <div className="highlight-container">
                <div className="header">
                    Real-Time Object Detection
                </div>
                <div className="body">
                    The very first and primary requirement of this program was to perform real-time detection and classification. With this came several features that needed to be implemented to make the most user-friendly and appealing design and use of the software. This included live video feed displayed within the application, instantaneous detection and classification, and an active and updating changelog corresponding with what the model is seeing for better information and data collection. By providing this need, there is no delay or post-processing in getting the information from our model. Instead, the user can observe and understand in real time what is happening as well as providing a time stamped collection of data for further analysis. 
                    <div className="buffer-small"></div>
                    <div className="italic">
                        Key Takeaways: Live camera streaming, instantaneous classification, and threaded frame processing.
                    </div>
                    <div className="buffer-small"></div>
                </div>

                <div className="header">
                    Fully Customizable Settings
                </div>
                <div className="body">
                    In order to deliver our program in a way that was the most user-friendly and dynamic, we integrated a full-fleshed settings option box that applies to almost all changes that would need to be changed from session to session. These settings cover the camera, storage, AI model, and advanced categories. Some features available are camera swapping and display options, save location and files, custom color support for bounding boxes and log messages, buffer speed adjustment, AI model swapping, confidence threshold scaling, as well as developer specific assistance. With even more integrated throughout our software, this settings menu allows the user to adjust whatever they please without having to access the source code, an essential consideration to successfully deliver the product in a packaged manner. 
                    <div className="buffer-small"></div>
                    <div className="italic">
                        Key Takeaways: Seamless backend to frontend connection and user-friendly tailoring.
                    </div>
                    <div className="buffer-small"></div>
                </div>

                <div className="header">
                    Custom Trained YOLO Model
                </div>
                <div className="body">
                    In order to accurately classify and track the disposable surgical staples provided to us, we needed to complete lengthy data collection and augmentation to continuously improve our dataset and therefore the AI model. Our team began with the YOLOv11 (You Only Look Once) model. Known for its speed and object detection capabilities, it was the best start for our project, despite its weakness regarding bias. In order to combat such, extensive data augmentation was performed to pad our dataset with all possible orientations and placements of the disposable, ensuring no matter where it was placed in the frame of the camera, or what orientation or rotation it was in, the model would still pick it up. We are happy to report that our model can accurately detect such objects with over 90% confidence in our best cases, supporting detection in less-than-ideal conditions such as low light, strange angles, and slight obstruction. 
                    <div className="buffer-small"></div>
                    <div className="italic">
                        Key Takeaways: Extensive data collection and augmentation techniques, unique training process, and high accuracy classification. 
                    </div>
                    <div className="buffer-small"></div>
                </div>
                
                <div className="header">
                    Robust Data Collection and Saving
                </div>
                <div className="body">
                    A key feature of our software is to ensure that whoever wishes to implement the technology into their own environment will be able to go back and access the recordings and log messages for further analysis. There are four files that are saved in each session: the After-Action Report in .txt format, the tracking log in .csv and .log formats, and the video in .mp4. The AAR provides a summary of the session and tracking algorithm in an easy-to-read configuration. The log is output in both files to allow for easy parsing when analyzing the data further. Then, the video is saved to allow for checking the output against the actions within frame. If the user wishes to save only some of these files, then it can be modified in the settings as well.  
                    <div className="buffer-small"></div>
                    <div className="italic">
                        Key Takeaways: Video, log, and AAR file writing and saving and file directory organization.
                    </div>
                    <div className="buffer-small"></div>
                </div>

                <div className="header">
                    Automatic Releases and Desktop Application
                </div>
                <div className="body">
                    To appeal to a wide variety of users, our project aimed to create a packaged release that is available open-source and OS-agnostic. We succeeded in such through the use of GitHub actions and JPackage. Not only is the software available for download as an .exe, .dmg, and .jar, but it also automatically generates a new application upon a new update and release. In doing so, the bulk of installing and updating is taken off of the user. This makes our application much more friendly to non-technical adopters, the only requirement being to download the single packaged application. 
                    <div className="buffer-small"></div>
                    <div className="italic">
                        Key Takeaways: OS-independent downloadable application, generated official releases upon main updates, GitHub protections and actions.
                    </div>
                    <div className="buffer-small"></div>
                </div>

                <div className="buffer"></div>
            </div>
        </>
    )
}

export default Highlights;