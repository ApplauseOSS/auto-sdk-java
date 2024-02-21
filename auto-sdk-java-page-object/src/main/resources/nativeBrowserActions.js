/*
 *
 * Copyright © 2024 Applause App Quality, Inc.
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *
 */

/**
 * Dispatches a Mouse/MSPointer/Touch Event to simulate pressing down on an element with a mouse or finger.
 *
 * @param element - An org.openqa.selenium.WebElement
 */
function dispatchDownEvent(element) {
    var downEvent;

    if("ontouchstart" in document) {
        console.log('Creating a touchstart event for a touch-enabled browser.');
        downEvent = document.createEvent('Event');
        downEvent.initEvent('touchstart',true,true);

        downEvent.touches = downEvent.targetTouches = [{
            clientX: 0,
            clientY: 0,
            identifier: 0
        }];

        downEvent.changedTouches = [{
            clientX: 0,
            clientY: 0,
            identifier: 0
        }];

    } else if(window.MSPointerEvent) {
        console.log('Creating an MSPointerDown event for IE.');
        downEvent = document.createEvent('MSPointerEvent');
        downEvent.initPointerEvent('MSPointerDown', true, true,window,0,0,0,0,0,false,false,false,false,0,null,0,0,0,0,0,0,0,0,0,0,0,true);
    } else {
        console.log('Creating a mousedown event.');
        downEvent = document.createEvent('MouseEvents');
        downEvent.initMouseEvent('mousedown',true,true,window,0,0,0,0,0,false,false,false,false,0,null);
    }

    console.log('Dispatching the event.');
    var result = element.dispatchEvent(downEvent);

    if(result) {
        console.log('The event was successfully dispatched');
    } else {
        console.log('The application prevented the event from being dispatched.');
    }
};

/**
 * Dispatches a Mouse/MSPointer/Touch Event to simulate dragging an element with a mouse or finger.
 *
 * @param element - An org.openqa.selenium.WebElement
 * @param positionX - The x-coordinate offset to move the element to.
 * @param positionY - The y-coordinate offset to move the element to.
 */
function dispatchMoveEvent(element, positionX, positionY) {
    var moveEvent;

    if("ontouchmove" in document) {
        console.log('Creating a touchmove event for a touch-enabled browser.');
        moveEvent = document.createEvent('Event');
        moveEvent.initEvent('touchmove',true,true);

        moveEvent.touches = moveEvent.targetTouches = [{
            clientX: positionX,
            clientY: positionY,
            identifier: 0
        }];

        moveEvent.changedTouches = [{
            clientX: positionX,
            clientY: positionY,
            identifier: 0
        }];

    } else if(window.MSPointerEvent) {
        console.log('Creating an MSPointerMove event for IE.');
        moveEvent = document.createEvent('MSPointerEvent');
        moveEvent.initPointerEvent('MSPointerMove', true, true,window,0,positionX,positionY,positionX,positionY,false,false,false,false,0,null,0,0,0,0,0,0,0,0,0,0,0,true);
    } else {
        console.log('Creating a mousemove event.');
        moveEvent = document.createEvent('MouseEvents');
        moveEvent.initMouseEvent('mousemove',true,true,window,0,positionX,positionY,positionX,positionY,false,false,false,false,0,null);
    }

    console.log('Dispatching the event.');
    var result = element.dispatchEvent(moveEvent);

    if(result) {
        console.log('The event was successfully dispatched');
    } else {
        console.log('The application prevented the event from being dispatched.');
    }
};

/**
 * Dispatches a Mouse/MSPointer/Touch Event to simulate depressing/releasing a mouse/finger from an element.
 *
 * @param element - An org.openqa.selenium.WebElement
 * @param positionX - The x-coordinate of the element's position.
 * @param positionY - The y-coordinate of the element's position.
 */
function dispatchUpEvent(element, positionX, positionY) {
    var upEvent;

    if("ontouchend" in document) {
        console.log('Creating a touchend event for a touch-enabled browser.');
        upEvent = document.createEvent('Event');
        upEvent.initEvent('touchend',true,true);

        upEvent.touches = upEvent.targetTouches = [{
            clientX: positionX,
            clientY: positionY,
            identifier: 0
        }];

        upEvent.changedTouches = [{
            clientX: positionX,
            clientY: positionY,
            identifier: 0
        }];

    } else if(window.MSPointerEvent) {
        console.log('Creating an MSPointerUp event for IE.');
        upEvent = document.createEvent('MSPointerEvent');
        upEvent.initPointerEvent('MSPointerUp', true, true,window,0,positionX,positionY,positionX,positionY,false,false,false,false,0,null,0,0,0,0,0,0,0,0,0,0,0,true);
    } else {
        console.log('Creating a mouseup event.');
        upEvent = document.createEvent('MouseEvents');
        upEvent.initMouseEvent('mouseup',true,true,window,0,positionX,positionY,positionX,positionY,false,false,false,false,0,null);
    }

    console.log('Dispatching the event.');
    var result = element.dispatchEvent(upEvent);

    if(result) {
        console.log('The event was successfully dispatched');
    } else {
        console.log('The application prevented the event from being dispatched.');
    }
};