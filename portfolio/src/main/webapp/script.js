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

let cat_photo_num = 0;
let cat_gallery_image = document.getElementById('cat-gallery-image');
let cat_gallery_subtitle = document.getElementById('cat-gallery-subtitle');
let cat_photos = [
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
const max_width = 800;
const max_height = 600;

changeCatPhoto(0);

function changeCatPhoto(num) {
  cat_gallery_subtitle.innerText = cat_photos[num]['subtitle'];
  if (cat_photos[num]['img'] == null) {
    let new_image = new Image();
    new_image.onload = function() {
      let width = new_image.width;
      let height = new_image.height;
      if (width / height > max_width / max_height) {
        let scale_factor = max_width / width;
        let new_height = height * scale_factor;
        new_image.style.width = max_width + 'px';
        new_image.style.height = new_height + 'px';
        new_image.style.marginTop = ((max_height - new_height) / 2) + 'px';
      } else {
        let scale_factor = max_height / height;
        let new_width = width * scale_factor;
        new_image.style.width = new_width + 'px';
        new_image.style.height = max_height + 'px';
        new_image.style.marginLeft = ((max_width - new_width) / 2) + 'px';
      }
      new_image.alt = cat_photos[num]['subtitle'];
      cat_photos[num][2] = new_image;
      cat_gallery_image.innerHTML = '';
      cat_gallery_image.appendChild(new_image);
    };
    new_image.src = '/images/' + cat_photos[num]['filename'];
  } else {
    cat_gallery_image.innerHTML = '';
    cat_gallery_image.appendChild(cat_photos[num]['img']);
  }
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
