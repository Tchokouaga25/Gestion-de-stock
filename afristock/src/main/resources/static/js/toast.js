(function () {
    "use strict";

    var container = null;

    function ensureContainer() {
        if (!container || !document.body.contains(container)) {
            container = document.getElementById("toast-container");
        }
        if (!container) {
            container = document.createElement("div");
            container.id = "toast-container";
            container.className = "toast-container";
            document.body.appendChild(container);
        }
        return container;
    }

    function show(message, type) {
        if (!message) {
            return;
        }
        var el = ensureContainer();
        var toast = document.createElement("div");
        toast.className = "toast toast--" + (type || "info");

        var text = document.createElement("span");
        text.className = "toast__message";
        text.textContent = message;

        var close = document.createElement("button");
        close.className = "toast__close";
        close.setAttribute("aria-label", "Fermer");
        close.textContent = "✕";
        close.addEventListener("click", function () {
            toast.remove();
        });

        toast.appendChild(text);
        toast.appendChild(close);
        el.appendChild(toast);

        window.setTimeout(function () {
            toast.remove();
        }, 5000);
    }

    // Convertit les bannières flash serveur (.alert-success / .alert-error) déjà rendues en toasts,
    // pour les pages qui n'ont pas encore de conteneur toast dédié. root permet de limiter le scan
    // au fragment fraîchement injecté après une navigation SPA (évite de re-scanner tout le document).
    function scan(root) {
        var scope = root || document;
        var banners = scope.querySelectorAll(".alert-success:not([data-toast-done]), .alert-error:not([data-toast-done])");
        banners.forEach(function (banner) {
            banner.setAttribute("data-toast-done", "true");
            var type = banner.classList.contains("alert-success") ? "success" : "error";
            show(banner.textContent.trim(), type);
            banner.style.display = "none";
        });
    }

    window.AfriToast = { show: show, scan: scan };

    document.addEventListener("DOMContentLoaded", function () {
        scan(document);
    });
})();
