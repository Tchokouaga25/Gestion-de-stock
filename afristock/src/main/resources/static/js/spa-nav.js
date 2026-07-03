(function () {
    "use strict";

    // Doit rester en miroir de NAV_PREFIXES dans SpaNavInterceptor.java.
    var NAV_PREFIXES = [
        ["/dashboard", "dashboard"],
        ["/sites", "sites"],
        ["/suppliers", "suppliers"],
        ["/customers", "customers"],
        ["/products/categories", "admin-categories"],
        ["/products", "products"],
        ["/sales", "sales"],
        ["/purchases", "purchases"],
        ["/stock", "stock"],
        ["/movements", "movements"],
        ["/profile", "profile"],
        ["/admin/users", "admin-users"],
        ["/subscription", "subscription"],
        ["/accounting", "accounting"],
        ["/hr", "hr"],
        ["/reports", "reports"],
        ["/super-admin/companies", "superadmin-companies"],
        ["/super-admin/plans", "superadmin-plans"],
        ["/super-admin/features", "superadmin-features"]
    ];

    var ACTIVE_CLASSES = "flex items-center gap-3 px-4 py-3 rounded-xl text-[13px] font-medium bg-blue-600 text-white shadow-lg shadow-blue-600/20";
    var INACTIVE_CLASSES = "flex items-center gap-3 px-4 py-3 rounded-xl transition-all duration-200 text-[13px] font-medium text-gray-500 hover:text-gray-900 hover:bg-gray-100";

    function computeActiveNav(pathname) {
        var best = null;
        for (var i = 0; i < NAV_PREFIXES.length; i++) {
            var prefix = NAV_PREFIXES[i][0];
            if (pathname === prefix || pathname.indexOf(prefix + "/") === 0) {
                if (!best || prefix.length > best[0].length) {
                    best = NAV_PREFIXES[i];
                }
            }
        }
        return best ? best[1] : "";
    }

    // Les <script> injectés via innerHTML ne s'exécutent jamais automatiquement (comportement
    // navigateur standard) : on les recrée pour forcer leur exécution après un swap SPA. Sans
    // ça, les pages avec un script de page (ex: sales/new.html) perdraient leur logique
    // dynamique lors d'une navigation en AJAX (elle fonctionnerait quand même au premier
    // chargement complet / à l'actualisation, puisque le navigateur exécute les <script>
    // présents dans le HTML initial).
    function reExecuteScripts(container) {
        container.querySelectorAll("script").forEach(function (oldScript) {
            var newScript = document.createElement("script");
            for (var i = 0; i < oldScript.attributes.length; i++) {
                var attr = oldScript.attributes[i];
                newScript.setAttribute(attr.name, attr.value);
            }
            newScript.textContent = oldScript.textContent;
            oldScript.parentNode.replaceChild(newScript, oldScript);
        });
    }

    function setActive(pathname) {
        var key = computeActiveNav(pathname);
        document.querySelectorAll("a[data-spa-link]").forEach(function (a) {
            a.className = (a.getAttribute("data-nav-key") === key) ? ACTIVE_CLASSES : INACTIVE_CLASSES;
        });
    }

    function navigate(url, push) {
        var main = document.getElementById("main-content");
        if (!main) {
            window.location.href = url;
            return;
        }
        main.setAttribute("aria-busy", "true");
        fetch(url, { headers: { "X-Spa-Nav": "true" }, credentials: "same-origin" })
            .then(function (res) {
                if (!res.ok || res.redirected) {
                    window.location.href = url;
                    return null;
                }
                return res.text();
            })
            .then(function (html) {
                if (html === null) {
                    return;
                }
                main.innerHTML = html;
                main.removeAttribute("aria-busy");
                reExecuteScripts(main);
                var titled = main.querySelector("[data-title]");
                if (titled) {
                    document.title = titled.getAttribute("data-title");
                }
                if (push) {
                    history.pushState({}, "", url);
                }
                setActive(new URL(url, window.location.origin).pathname);
                window.scrollTo(0, 0);
            })
            .catch(function () {
                window.location.href = url;
            });
    }

    document.addEventListener("click", function (e) {
        if (e.defaultPrevented || e.button !== 0 || e.metaKey || e.ctrlKey || e.shiftKey || e.altKey) {
            return;
        }
        var link = e.target.closest("a[data-spa-link]");
        if (!link) {
            return;
        }
        e.preventDefault();
        navigate(link.getAttribute("href"), true);
    });

    window.addEventListener("popstate", function () {
        navigate(location.pathname + location.search, false);
    });
})();
