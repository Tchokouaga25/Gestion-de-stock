(function () {
    "use strict";

    // Délégation sur document : la topbar vit dans #main-content sur certaines pages et est donc
    // recréée à chaque navigation SPA — un binding direct serait perdu après navigation.
    document.addEventListener("click", function (e) {
        var toggle = e.target.closest("#notif-toggle");
        var panel = document.getElementById("notif-panel");
        if (!panel) {
            return;
        }
        if (toggle) {
            panel.classList.toggle("open");
            return;
        }
        if (!panel.contains(e.target)) {
            panel.classList.remove("open");
        }
    });
})();
