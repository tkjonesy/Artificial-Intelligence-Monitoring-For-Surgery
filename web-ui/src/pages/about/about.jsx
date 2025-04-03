import React, { useState } from "react";
import {Link} from "react-router-dom";
import "../../index.css";

function About() {
    return (
        <>
            <div className="title-container">
            <div className="title">
                About the Team
            </div>

            <div className="subheading">
                If you have any questions about the project, please reach out to our team.
            </div>
            </div>
            
            <div class="wave">
            <svg xmlns="http://www.w3.org/2000/svg" viewBox="0 20 1440 120"><path fill="#000000" fill-opacity="1" d="M0,32L60,32C120,32,240,32,360,48C480,64,600,96,720,96C840,96,960,64,1080,48C1200,32,1320,32,1380,32L1440,32L1440,0L1380,0C1320,0,1200,0,1080,0C960,0,840,0,720,0C600,0,480,0,360,0C240,0,120,0,60,0L0,0Z"></path></svg>
            </div>

            <span className="person-container">
            <div className="person">
            <div className="headerc">
                Rachel Leiner
            </div>

            <div className="header3">
                Project Manager, Developer
            </div>

            <div className="header4">
                rachelleiner02@gmail.com
            </div>
            </div>

            <div className="person">
            <div className="headerc">
                Trever Jones
            </div>

            <div className="header3">
                ML/CV Engineer​
            </div>

            <div className="header4">
                tkjones123456@gmail.com
            </div>
            </div>

            <div className="person">
            <div className="headerc">
                Jacob McKiernan​
            </div>

            <div className="header3">
                TBA
            </div>

            <div className="header4">
                jacob.b.mckiernan@gmail.com
            </div>
            </div>
            </span>

            <span className="person-container">
            <div className="person">
            <div className="headerc">
                Nicholas Aristizabal​
            </div>

            <div className="header3">
                Backend Developer – AI Integration & Core​
            </div>

            <div className="header4">
                nicholas.aristizabal@gmail.com
            </div>
            </div>

            <div className="person">
            <div className="headerc">
                Hunter Herbst​
            </div>

            <div className="header3">
                Frontend Developer - UI/UX & Camera Integration​
            </div>

            <div className="header4">
                hunter.d.herbst2k@gmail.com
            </div>
            </div>

            <div className="person">
            <div className="headerc">
                Gabriel Rechdan
            </div>

            <div className="header3">
                Backend Developer – Software development ​
            </div>

            <div className="header4">
                Grechdan4453@gmail.com
            </div>
            </div>
            </span>

        </>
    )
}

export default About;