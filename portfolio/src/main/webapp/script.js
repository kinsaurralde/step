// Copyright 2020 Google LLC
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

changeCatPhoto(0);

/**
 * Change image dimensions while keeping aspect ratio
 * @param {Image} newImage
 * @param {int} maxWidth
 * @param {int} maxHeight
 */
function scaleImage(newImage, maxWidth, maxHeight) {
  let width = newImage.width;
  let height = newImage.height;
  if (width / height >
      maxWidth / maxHeight) { /* Set width to maxWidth and scale height */
    let scaleFactor = maxWidth / width;
    let newHeight = height * scaleFactor;
    newImage.style.width = maxWidth + 'px';
    newImage.style.height = newHeight + 'px';
    newImage.style.marginTop =
        ((maxHeight - newHeight) / 2) + 'px'; /* Center photo vertically */
  } else { /* Set height to maxHeight and scale width */
    let scaleFactor = maxHeight / height;
    let newWidth = width * scaleFactor;
    newImage.style.width = newWidth + 'px';
    newImage.style.height = maxHeight + 'px';
    newImage.style.marginLeft =
        ((maxWidth - newWidth) / 2) + 'px'; /* Center photo horizontally */
  }
}

/**
 * Switches image in cat gallery
 * @param {number} num
 */
function changeCatPhoto(num) {
  if (catPhotos[num]['img'] == null) { /* Photo only needs to be loaded once */
    let newImage = new Image();
    newImage.onload = function() {
      scaleImage(newImage, 800, 600);
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

/**
 * Switch theme between light and dark mode
 */
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

/**
 * Creates div filled with comment content
 * @param {object} data
 * @return {div}
 */
function createCommentDiv(data) {
  const div = document.createElement('div');
  div.className = 'comment-div';
  const name = document.createElement('h4');
  if (data['hideEmail']) {
    name.textContent = data['name'];
  } else {
    name.textContent = data['email'];
  }
  div.appendChild(name);
  const text = document.createElement('p');
  text.textContent = data['text'];
  div.appendChild(text);
  if (data['imageKey'].length > 0 && data['imageKey'] != 'null\n') {
    fetch('/blobstore-get?blob-key=' + data['imageKey']).then((response) => {
      console.debug(response);
      let newImage = new Image();
      newImage.onload = function() {
        scaleImage(newImage, 400, 300);
        newImage.alt = data['text'];
        newImage.style.display = 'block';
      };
      newImage.style.display = 'none';
      newImage.src = response['url'];
      div.appendChild(newImage);
    })
  }
  const timestamp = document.createElement('h6');
  const datetime = new Date(data['timestamp']);
  timestamp.textContent = datetime.toLocaleString('en-US');
  div.appendChild(timestamp);
  return div;
}

/**
 * Request list of comments from server and display them
 */
function getComments() {
  const numComments = document.getElementById('num-comments').value;
  const page = document.getElementById('page').value;
  const sort = document.getElementById('comment-sort').value;
  if (numComments === '' || page === '' || sort === '') {
    return;
  }
  const queryString =
      'num-comments=' + numComments + '&page=' + page + '&sort=' + sort;
  fetch(
      '/data' +
      '?' + queryString)
      .then(response => response.json())
      .then((comments => {
        console.debug(comments);
        document.getElementById('page').max =
            (comments.length > 0) ? page + 1 : page;
        const commentsContainer = document.getElementById('comments-container');
        commentsContainer.innerText = '';
        for (let i in comments) {
          commentsContainer.appendChild(createCommentDiv(comments[i]));
        }
      }));
}

/**
 * Tell server to delete all comments
 */
function deleteComments() {
  const request = new Request('/delete-data', {method: 'POST'});
  fetch(request).then(function() {
    getComments();
  });
}

/**
 * Sends comment data to server
 * @param {String} imageKey
 */
function sendForm(imageKey) {
  let formData = new FormData();
  formData.append(
      'comment-text', document.getElementById('comment-text').value);
  formData.append(
      'comment-name', document.getElementById('comment-name').value);
  formData.append('comment-image-key', imageKey);
  const request = new Request('/data', {method: 'POST', body: formData});
  document.getElementById('comment-text').value = '';
  fetch(request).then(() => {getComments()});
}

/**
 * Uploads image file to given url
 * @param {String} imageUploadUrl
 */
function uploadPhoto(imageUploadUrl) {
  console.debug('imageUploadUrl', imageUploadUrl);
  let fileData = new FormData();
  fileData.append('image', document.getElementById('comment-image').files[0]);
  const fileUploadRequest =
      new Request(imageUploadUrl, {method: 'POST', body: fileData});
  fetch(fileUploadRequest)
      .then((response) => {
        return response.json();
      })
      .then((responseData) => {
        console.debug('fileData', responseData['imageKey'], responseData['imageLabels']);
        sendForm(responseData['imageKey']);
      })
}

/**
 * Send new comment to server to add
 */
function addComment() {
  fetch('/blobstore-upload')
      .then((response) => {
        return response.text();
      })
      .then((imageUploadUrl) => {
        uploadPhoto(imageUploadUrl);
      });
}

/**
 * Shows login link if not logged in and logout link if logged in
 */
function checkLoginStatus() {
  fetch('/login-status').then(response => response.json()).then((status => {
    console.debug(status, status.email);
    if (status.loggedIn) {
      document.getElementById('login-link').href = status.url;
      document.getElementById('login-button').value =
          'Logout from ' + status.email;
      document.getElementById('add-comments').style.display = 'block';
    } else {
      document.getElementById('login-link').href = status.url;
      document.getElementById('login-button').value = 'Login';
      document.getElementById('add-comments').style.display = 'none';
    }
  }));
}

/**
 * Gets comments and login status when page is loaded
 */
function pageLoad() {
  getComments();
  checkLoginStatus();
}
