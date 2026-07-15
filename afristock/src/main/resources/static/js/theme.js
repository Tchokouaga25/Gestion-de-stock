(function () {
    "use strict";

    var STORAGE_KEY = "afristock-theme";

    function apply(theme) {
        document.documentElement.classList.toggle("dark", theme === "dark");
    }

    function toggle() {
        var next = document.documentElement.classList.contains("dark") ? "light" : "dark";
        window.localStorage.setItem(STORAGE_KEY, next);
        apply(next);
    }

    apply(window.localStorage.getItem(STORAGE_KEY));

    // Délégation sur document : le bouton #theme-toggle vit dans #main-content sur certaines
    // pages et est donc recréé à chaque swap SPA — un binding direct serait perdu après navigation.
    document.addEventListener("click", function (e) {
        var btn = e.target.closest("#theme-toggle");
        if (btn) {
            toggle();
        }
    });
})();
