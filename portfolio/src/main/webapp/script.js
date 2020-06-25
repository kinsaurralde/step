// Copyright 2019 Google LLC
//
// Licensed under the Apache License, Version 2.0 (the "License");
// you may not use this file except in compliance with the License.
// You may obtain a copy of the License at
//
//     https://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing, software
// distributed under the License is distributed on an "AS IS" BASIS,
// WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
// See the License for the specific language governing permissions and
// limitations under the License.

/**
 * Adds a random greeting to the page.
 */
function addRandomGreeting() {
  const greetings =
      ['Hi', 'Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!', 'Greeting', "According to all known laws of aviation, there is no way a bee should be able to fly. Its wings are too small to get its fat little body off the ground. The bee, of course, flies anyway because bees don't care what humans think is impossible."];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

let cat_photo_num = 0;
let cat_gallery_image = document.getElementById("cat-gallery-image");
let cat_gallery_subtitle = document.getElementById("cat-gallery-subtitle");
const cat_photos = [
    ["simba.jpg", "This is Simba sleeping part 1"],
    ["simba_2.jpg", "This is Simba sleeping part 2"],
    ["IMG_7812.JPG", "This is Simba sleeping part 3"],
    ["IMG_6657.jpeg", "This is Simba sleeping part 4"],
];

function changeCatPhoto(num) {
    cat_gallery_image.src = "/images/" + cat_photos[num][0];
    cat_gallery_subtitle.innerText = cat_photos[num][1];
}

function previousCatPhoto() {
    if (cat_photo_num > 0) {
        cat_photo_num -= 1;
    } else {
        cat_photo_num = cat_photos.length - 1;
    }
    changeCatPhoto(cat_photo_num);
}

function nextCatPhoto() {
    cat_photo_num = (cat_photo_num + 1) % cat_photos.length;
    changeCatPhoto(cat_photo_num);
}

function randomCatPhoto() {
    cat_photo_num = Math.floor(Math.random() * cat_photos.length);
    changeCatPhoto(cat_photo_num);
}
