// PWA Service Worker - HongGuoTV
const CACHE_NAME = 'hongguo-shell-v2.2';
const ASSETS = [
  '/',
  '/manifest.json',
  '/public/icon.svg'
];

self.addEventListener('install', (e) => {
  self.skipWaiting();
  e.waitUntil(
    caches.open(CACHE_NAME).then((cache) => cache.addAll(ASSETS)).catch(() => {})
  );
});

self.addEventListener('activate', (e) => {
  e.waitUntil(
    caches.keys().then((keys) =>
      Promise.all(keys.map((k) => (k !== CACHE_NAME ? caches.delete(k) : null)))
    ).then(() => self.clients.claim())
  );
});

self.addEventListener('message', (event) => {
  if (event.data === 'SKIP_WAITING') {
    self.skipWaiting();
  }
});

self.addEventListener('fetch', (e) => {
  // Only handle GET requests
  if (e.request.method !== 'GET') return;

  // Ignore non-http/https protocols (e.g. chrome-extension://, moz-extension://)
  if (!e.request.url.startsWith('http://') && !e.request.url.startsWith('https://')) return;

  let url;
  try {
    url = new URL(e.request.url);
  } catch {
    return;
  }

  // Only handle same-origin requests; bypass third-party requests, extensions, and all /api/ endpoints
  if (url.origin !== self.location.origin || url.pathname.startsWith('/api/')) return;

  // Network-First for HTML/page navigation, fallback to cache when offline
  if (e.request.mode === 'navigate' || e.request.destination === 'document') {
    e.respondWith(
      fetch(e.request)
        .then((res) => {
          if (res && res.status === 200) {
            const clone = res.clone();
            caches.open(CACHE_NAME).then((cache) => cache.put(e.request, clone)).catch(() => {});
          }
          return res;
        })
        .catch(() => caches.match(e.request).then((cached) => cached || caches.match('/')))
    );
    return;
  }

  // Network-First for same-origin static shell assets with cache update
  e.respondWith(
    fetch(e.request)
      .then((res) => {
        if (res && res.status === 200) {
          const clone = res.clone();
          caches.open(CACHE_NAME).then((cache) => cache.put(e.request, clone)).catch(() => {});
        }
        return res;
      })
      .catch(() => caches.match(e.request))
  );
});
