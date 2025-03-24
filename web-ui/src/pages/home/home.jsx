import React, { useState } from "react";
import {Link} from "react-router-dom";
import "../../index.css";

function Home() {
    return (
        <>
            <div className="title-container">
            <div className="title">
                AIM(s): Artificial Intelligence Monitoring for Surgery
            </div>

            <div className="subheading">
                A full system solution to track surgical instrument usage in the operating room.
            </div>
            </div>
            
            <div class="wave">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 20 1440 120"><path fill="#000000" fill-opacity="1" d="M0,32L60,32C120,32,240,32,360,48C480,64,600,96,720,96C840,96,960,64,1080,48C1200,32,1320,32,1380,32L1440,32L1440,0L1380,0C1320,0,1200,0,1080,0C960,0,840,0,720,0C600,0,480,0,360,0C240,0,120,0,60,0L0,0Z"></path></svg>
            </div>

            <div className="header">
                What is AIM(s)?
            </div>

            <div className="body">
                The goal of AIM(s) is to help combat surgical waste and excess costs within the operating room by tracking disposable staple loads utilized in robotic surgeries to close the incisions. Prior to surgery, the necessary equipment for that operation must be laid out for the surgical team. These tool types and amounts are then indicated in a predetermined checklist. The staple loads are one-use only; once opened, they must be used or thrown away as they are now unsterile. Since there is a standardized amount to be opened, issues arise where not all the loads are used. Additionally, workers are less likely to report their mistakes, leading to concerns with human error.
                <br /> <br />
                AIM(s) is a full system solution that can be integrated into the operating room, utilizing AI to track in real time the number of loads that are used throughout a surgery. The results of such are logged throughout the operation and output to a report for further analysis and post-processing. At approximately $250 per load with 1200 surgeries annually, this tool can quickly save thousands of dollars for the hospital and reduce unnecessary waste in surgery.
            </div>

            <div className="header">
                Project Problem
            </div>

            <div className="body">
                When setting up an operating room for surgery, the technician sets up the sterile feed with a predefined number of instruments necessary. Then, throughout the procedure, if more tools are required, the technician will add them to the field to be used. For our specific use case, AIM(s) focuses on tracking the disposable staples used during robotic bariatric surgeries. 
                <br /> <br />
                The concerns that arise in regard to this process are unnecessary waste and cost due to human error or other outlying factors. If a technician accidentally opens too many staples, or something goes wrong in a procedure, additional staple loads may be opened. Since these are one-use materials, the entire staple load will go to waste.  
            </div>

            <div className="header">
                Proposed Solution
            </div>

            <div className="body">
            The goal of this software is therefore to track the use of these surgical disposables in order to gather data to further reduce waste and cost in the long term. The hope is, with this data, the hospital can modify the minimum number of staples required at the start of the surgery. With these loads costing about $250 per, this can work to save thousands across many surgeries. 
            </div>

            <div className="buffer"></div>
        </>
    )
}

export default Home;