import React, { useState } from "react";
import {Link} from "react-router-dom";
import "../../index.css";
import { ImageGallery } from "react-image-grid-gallery";
import s1 from "../../assets/surgery.jpg";
import d11 from "../../assets/demo1.jpg";
import d21 from "../../assets/demo2.jpg";
import d12 from "../../assets/demo1_2.jpg";
import d13 from "../../assets/demo1_3.jpg";
import d14 from "../../assets/demo1_4.jpg";
import d15 from "../../assets/demo1_5.jpg";
import d16 from "../../assets/demo1_6.jpg";
import d22 from "../../assets/demo2_2.jpg";
import g1 from "../../assets/group.jpg";

function Adoption() {

    const imagesArray = [
        
        {
          id: "1",
          alt: "Image2's alt text",
          caption: "Testing the custom YOLO model.",
          src: d11,
        },
        {
            id: "2",
            alt: "Image3's alt text",
            caption: "1st demo with OH.",
            src: d12,
          },
          {
            id: "3",
            alt: "Image3's alt text",
            caption: "AIM(s) during the live surgery demo.",
            src: d21,
          },
          {
            id: "4",
            alt: "Image3's alt text",
            caption: "1st demo with OH.",
            src: d16,
          },
          
          {
            id: "5",
            alt: "Image3's alt text",
            caption: "Testing the custom YOLO model.",
            src: d14,
          },
          {
            id: "6",
            alt: "Image3's alt text",
            caption: "Testing the custom YOLO model.",
            src: d15,
          },
          
          {
            id: "7",
            alt: "Image3's alt text",
            caption: "Demonstrating the software.",
            src: d13,
          },
          {
            id: "8",
            alt: "Image1's alt text",
            caption: "Live surgery demo.",
            src: s1,
          },
          {
            id: "9",
            alt: "Image3's alt text",
            caption: "2nd demo with OH.",
            src: d22,
          },
          {
            id: "10",
            alt: "Image3's alt text",
            caption: "Our team with Dr. Aguirre, Dr. Brattain, and Dr. Sanchez",
            src: g1,
          },
      ];

    return (
        <>
            <div className="title-container">
            <div className="title">
                Adoption Efforts
            </div>

            <div className="subheading">
                Successful deployment of the software in a hospital setting.
            </div>
            </div>
            
            <div class="wave">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 20 1440 120"><path fill="#000000" fill-opacity="1" d="M0,32L60,32C120,32,240,32,360,48C480,64,600,96,720,96C840,96,960,64,1080,48C1200,32,1320,32,1380,32L1440,32L1440,0L1380,0C1320,0,1200,0,1080,0C960,0,840,0,720,0C600,0,480,0,360,0C240,0,120,0,60,0L0,0Z"></path></svg>
            </div>

            <div className="adopt-container">
                <div className="header">
                    Working with Orlando Health
                </div>
                <div className="body">
                    At the start of this project, our team was unsure of just how far we could go with adoption and deployment efforts in a hospital setting. However, thanks to our sponsor Dr. Laura Brattain, we were able to get in contact with Dr. Alexis Sanchez, the senior director of the Robotic Surgery Program at Orlando Health. Once we made contact with Dr. Sanchez, our team was able to move in the direction of delivering our program in a real-world scenario. Over the past few months, we have worked rigorously to create user-friendly and detailed software and model that could be adapted to usage in the operating room. We performed four demos with Orlando Health, two of which were during live surgeries at the hospital.
                </div>
                <div className="buffer-small"></div>

                <div className="header2">
                    Live Surgery Demo
                </div>
                <div className="body">
                    Thanks to Dr. Sanchez, we were given the opportunity to observe a surgery and test our software in tandem. The procedure in which this testing occurred was a laparoscopic single anastomosis duodenal switch. On the day of our demonstration, the team was able to run two demos during two surgeries. This allowed us to observe how feasible using this software during a surgery would be, errors that may occur, and what would have needed to have been tweaked moving forward. Fortunately, the demo was an astounding success, with small issues in human error or less than optimal set up and processes. However, after addressing these issues between surgeries, we were able to run a demonstration that was near perfect. This proved the practicality of the product and set us up for future success.
                </div>
                <div className="buffer-small"></div>

                <div className="header2">
                    Moving Forward
                </div>
                <div className="body">
                    At the time this project has been completed, our team is in the process of helping Orlando Health to acquire the appropriate materials in order to set up and run our software. Our team plans to help with integrating the product onto their device and operating room. Our hope is that our program will live on past this project and become successfully adopted and integrated into the hospital setting. 
                </div>
            </div>

            <div className="gallery">
                <ImageGallery imagesInfoArray={imagesArray} gapSize={24} columnWidth={350}/>
            </div>
            
        </>
    )
}

export default Adoption;