/* sw.js - Base MDM (AngularJS) PWA service worker
 * Strategy:
 * - Precache app shell (index + static assets)
 * - Network-first for navigations (SPA) so you get latest UI when online
 * - Stale-while-revalidate for static assets (lib/css/js/images)
 * - Network-only (no cache) for API/auth endpoints to avoid stale data
 */

const VERSION = "v1.0.0";
const STATIC_CACHE = `base-mdm-static-${VERSION}`;
const RUNTIME_CACHE = `base-mdm-runtime-${VERSION}`;

// Adjust to your real paths.
// Keep this list conservative to avoid caching huge/volatile assets.
const PRECACHE_URLS = [
  "/",
  "/index.html",
  "/manifest.json",
  "/css/main.css",
  "/images/favicon.ico",
  "/images/logo144.png",
  "/images/logo192.png",
  "/images/logo512.png",

  // Angular app entry points (add/remove based on your deployment)
  "/app/app.js",
  "/app/spinner.js"
];

self.addEventListener("install", (event) => {
  event.waitUntil(
    (async () => {
      const cache = await caches.open(STATIC_CACHE);
      // Using {cache: 'reload'} helps bypass any HTTP cache on first install.
      await cache.addAll(PRECACHE_URLS.map((u) => new Request(u, { cache: "reload" })));
      self.skipWaiting();
    })()
  );
});

self.addEventListener("activate", (event) => {
  event.waitUntil(
    (async () => {
      const keys = await caches.keys();
      await Promise.all(
        keys.map((key) => {
          if (key !== STATIC_CACHE && key !== RUNTIME_CACHE) {
            return caches.delete(key);
          }
        })
      );
      await self.clients.claim();
    })()
  );
});

function isApiRequest(url) {
  // Tune these rules to your backend routes.
  // Common patterns for MDM panels: /rest, /api, /auth, /oauth, /ws
  return (
    url.pathname.startsWith("/api") ||
    url.pathname.startsWith("/rest") ||
    url.pathname.startsWith("/auth") ||
    url.pathname.startsWith("/oauth") ||
    url.pathname.startsWith("/ws") ||
    url.pathname.startsWith("/socket")
  );
}

function isStaticAsset(url) {
  return (
    url.pathname.startsWith("/lib/") ||
    url.pathname.startsWith("/app/") ||
    url.pathname.startsWith("/css/") ||
    url.pathname.startsWith("/images/") ||
    url.pathname.startsWith("/icon/") ||
    url.pathname.endsWith(".js") ||
    url.pathname.endsWith(".css") ||
    url.pathname.endsWith(".png") ||
    url.pathname.endsWith(".jpg") ||
    url.pathname.endsWith(".jpeg") ||
    url.pathname.endsWith(".svg") ||
    url.pathname.endsWith(".ico") ||
    url.pathname.endsWith(".woff") ||
    url.pathname.endsWith(".woff2") ||
    url.pathname.endsWith(".ttf")
  );
}

async function networkFirst(request) {
  const cache = await caches.open(RUNTIME_CACHE);
  try {
    const response = await fetch(request);
    // Cache only successful basic responses
    if (response && response.status === 200 && response.type === "basic") {
      cache.put(request, response.clone());
    }
    return response;
  } catch (err) {
    const cached = await cache.match(request);
    if (cached) return cached;

    // Fallback to cached index for SPA navigations
    if (request.mode === "navigate") {
      const staticCache = await caches.open(STATIC_CACHE);
      const index = await staticCache.match("/index.html");
      if (index) return index;
    }
    throw err;
  }
}

async function staleWhileRevalidate(request) {
  const cache = await caches.open(RUNTIME_CACHE);
  const cached = await cache.match(request);

  const fetchPromise = fetch(request)
    .then((response) => {
      if (response && response.status === 200 && (response.type === "basic" || response.type === "cors")) {
        cache.put(request, response.clone());
      }
      return response;
    })
    .catch(() => cached); // if offline, use cached if available

  return cached || fetchPromise;
}

self.addEventListener("fetch", (event) => {
  const request = event.request;

  // Only handle GET
  if (request.method !== "GET") return;

  const url = new URL(request.url);

  // Only same-origin
  if (url.origin !== self.location.origin) return;

  // Never cache API/auth to avoid stale data issues
  if (isApiRequest(url)) {
    event.respondWith(fetch(request));
    return;
  }

  // SPA navigations: network-first with offline fallback to index.html
  if (request.mode === "navigate") {
    event.respondWith(networkFirst(request));
    return;
  }

  // Static assets: stale-while-revalidate
  if (isStaticAsset(url)) {
    event.respondWith(staleWhileRevalidate(request));
    return;
  }

  // Default: network-first
  event.respondWith(networkFirst(request));
});
