/**
 * Part of Waytous <http://waytous.net>
 * Copyright (C) Edeqa LLC <http://www.edeqa.com>
 *
 * Version 1.${SERVER_BUILD}
 * Created 1/20/17.
 */
var CACHE_NAME = "${APP_NAME}-v${SERVER_BUILD}";
var urlsToCache = [
  "/",
  "/css/tracking.css",
  "/images/logo.png",
  "/images/marker.svg",
  "/js/helpers/Constants.js",
  "/js/helpers/Edequate.js",
  "/js/helpers/MyUser.js",
  "/js/helpers/MyUsers.js",
  "/js/helpers/NoSleep.js",
  "/js/helpers/TrackingFB.js",
  "/js/helpers/Utils.js",
  "/js/tracking/AddressHolder.js",
  "/js/tracking/ButtonHolder.js",
  "/js/tracking/CameraHolder.js",
  "/js/tracking/DistanceHolder.js",
  "/js/tracking/DrawerHolder.js",
  "/js/tracking/FabHolder.js",
  "/js/tracking/GpsHolder.js",
  "/js/tracking/HelpHolder.js",
  "/js/tracking/Main.js",
  "/js/tracking/MapHolder.js",
  "/js/tracking/MarkerHolder.js",
  "/js/tracking/MessagesHolder.js",
  "/js/tracking/NavigationHolder.js",
  "/js/tracking/OptionHolder.js",
  "/js/tracking/PlaceHolder.js",
  "/js/tracking/PropertiesHolder.js",
  "/js/tracking/SavedLocationHolder.js",
  "/js/tracking/SocialHolder.js",
  "/js/tracking/StreetViewHolder.js",
  "/js/tracking/TrackHolder.js",
  "/js/tracking/TrackingHolder.js",
  "/js/tracking/WelcomeHolder.js",
  "/locales/resources.en-us.json"
];

self.addEventListener("install", function(event) {
  event.waitUntil(
    caches.open(CACHE_NAME)
      .then(function(cache) {
        console.log("Opened cache");
        return cache.addAll(urlsToCache);
      })
  );
});

self.addEventListener("fetch", function(event) {

    if (event.request.method !== 'GET') {
    /* If we don't block the event as shown below, then the request will go to
       the network as usual.
    */
//        console.log('WORKER: fetch event ignored.', event.request.method, event.request.url);
        return;
    }
  event.respondWith(
    caches.match(event.request)
      .then(function(response) {
        if (response) {
//            console.log("Return cached",event.request);
            return response;
        }

        var fetchRequest = event.request.clone();

        return fetch(fetchRequest).then(
          function(response) {
            if(!response || response.status !== 200 || response.type !== "basic") {
//                console.log("Return cached",response);
                return response;
            }

            var responseToCache = response.clone();

            caches.open(CACHE_NAME).then(function(cache) {
                cache.put(event.request, responseToCache);
            });
//            console.log("Return cached",response);
            return response;
          }
        );
      })
    );
});

self.addEventListener("activate", function(event) {
  var cacheWhitelist = [CACHE_NAME];
  event.waitUntil(
    caches.keys().then(function(cacheNames) {
      return Promise.all(
        cacheNames.map(function(cacheName) {
          if (cacheWhitelist.indexOf(cacheName) === -1) {
            return caches.delete(cacheName);
          }
        })
      );
    })
  );
});