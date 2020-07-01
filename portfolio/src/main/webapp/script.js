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
  const greetings = [
    'Hi', 'Hello world!', '¡Hola Mundo!', '你好，世界！', 'Bonjour le monde!',
    'Greeting',
    'According to all known laws of aviation, there is no way a bee should be able to fly. Its wings are too small to get its fat little body off the ground. The bee, of course, flies anyway because bees don\'t care what humans think is impossible.'
  ];

  // Pick a random greeting.
  const greeting = greetings[Math.floor(Math.random() * greetings.length)];

  // Add it to the page.
  const greetingContainer = document.getElementById('greeting-container');
  greetingContainer.innerText = greeting;
}

let catPhotoNum = 0;
let catGalleryImage = document.getElementById('cat-gallery-image');
let catGallerySubtitle = document.getElementById('cat-gallery-subtitle');
let catPhotos = [
  {'filename': 'simba.jpg', 'subtitle': 'Simba sleeping part 1', 'img': null},
  {'filename': 'simba_2.jpg', 'subtitle': 'Simba sleeping part 2', 'img': null},
  {'filename': 'IMG_1346.JPG', 'subtitle': 'Simba in a box', 'img': null},
  {'filename': 'IMG_1950.JPG', 'subtitle': 'Pepper eating food', 'img': null},
  {
    'filename': 'IMG_3973.JPG',
    'subtitle': 'Simba sleeping part 3',
    'img': null
  },
  {'filename': 'IMG_4355.JPG', 'subtitle': 'Annoyed', 'img': null},
  {'filename': 'IMG_7812.JPG', 'subtitle': 'Looking at something', 'img': null},
  {'filename': 'IMG_6657.jpeg', 'subtitle': 'Christmas costume', 'img': null},
];
const MAX_WIDTH = 800;
const MAX_HEIGHT = 600;

changeCatPhoto(0);

/**
 * Switches image in cat gallery
 * @param {number} num
 */
function changeCatPhoto(num) {
  if (catPhotos[num]['img'] == null) { /* Photo only needs to be loaded once */
    let newImage = new Image();
    newImage.onload = function() {
      let width = newImage.width;
      let height = newImage.height;
      if (width / height > MAX_WIDTH /
              MAX_HEIGHT) { /* Set width to MAX_WIDTH and scale height */
        let scaleFactor = MAX_WIDTH / width;
        let newHeight = height * scaleFactor;
        newImage.style.width = MAX_WIDTH + 'px';
        newImage.style.height = newHeight + 'px';
        newImage.style.marginTop =
            ((MAX_HEIGHT - newHeight) / 2) + 'px'; /* Center photo vertically */
      } else { /* Set height to MAX_HEIGHT and scale width */
        let scaleFactor = MAX_HEIGHT / height;
        let newWidth = width * scaleFactor;
        newImage.style.width = newWidth + 'px';
        newImage.style.height = MAX_HEIGHT + 'px';
        newImage.style.marginLeft =
            ((MAX_WIDTH - newWidth) / 2) + 'px'; /* Center photo horizontally */
      }
      newImage.alt = catPhotos[num]['subtitle'];
      catPhotos[num]['img'] = newImage;
      catGalleryImage.innerHTML = '';
      catGallerySubtitle.innerText = catPhotos[num]['subtitle'];
      catGalleryImage.appendChild(newImage);
    };
    newImage.src = '/images/' + catPhotos[num]['filename'];
  } else {
    catGalleryImage.innerHTML = '';
    catGallerySubtitle.innerText = catPhotos[num]['subtitle'];
    catGalleryImage.appendChild(catPhotos[num]['img']);
  }
}

/**
 * Sets catPhotoNum to previous photo number then changes to that photo
 */
function previousCatPhoto() {
  if (catPhotoNum > 0) {
    catPhotoNum -= 1;
  } else {
    catPhotoNum = catPhotos.length - 1;
  }
  changeCatPhoto(catPhotoNum);
}

/**
 * Sets catPhotoNum to next photo number then changes to that photo
 */
function nextCatPhoto() {
  catPhotoNum = (catPhotoNum + 1) % catPhotos.length;
  changeCatPhoto(catPhotoNum);
}

/**
 * Sets catPhotoNum to a random photo number then changes to that photo
 */
function randomCatPhoto() {
  catPhotoNum = Math.floor(Math.random() * catPhotos.length);
  changeCatPhoto(catPhotoNum);
}

function toggleTheme() {
  let cssVars = document.getElementsByTagName('html')[0].style;
  let curTheme = cssVars.getPropertyValue('--background-color');
  let newTheme = 'dark';
  if (curTheme == 'var(--dark-background)') {
    newTheme = 'light';
    document.getElementById('github-icon').src =
        '/icons/GitHub-Mark-120px-plus.png';
  } else {
    document.getElementById('github-icon').src =
        '/icons/GitHub-Mark-Light-120px-plus.png';
  }
  cssVars.setProperty(
      '--background-color', 'var(--' + newTheme + '-background)');
  cssVars.setProperty('--text-color', 'var(--' + newTheme + '-text)');
  cssVars.setProperty('--input-color', 'var(--' + newTheme + '-input-color)');
  cssVars.setProperty(
      '--input-border-color', 'var(--' + newTheme + '-input-border-color)');
  cssVars.setProperty('--menu-color', 'var(--' + newTheme + '-menu-color)');
}
