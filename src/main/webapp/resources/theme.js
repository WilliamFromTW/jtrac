/**
 * JTrac Three-State Theme Switcher (☀️ Light -> 🌙 Dark -> 🖥️ System)
 */
(function() {
    function getStoredTheme() {
        try {
            return localStorage.getItem('jtrac-theme') || 'system';
        } catch(e) {
            return 'system';
        }
    }

    function isSystemDark() {
        try {
            return !!(window.matchMedia && window.matchMedia('(prefers-color-scheme: dark)').matches);
        } catch(e) {
            return false;
        }
    }

    function getThemeIcon(mode) {
        if (mode === 'light') return '☀️';
        if (mode === 'dark') return '🌙';
        return '🖥️';
    }

    function updateThemeUI(mode) {
        var icon = getThemeIcon(mode);
        var iconEls = document.querySelectorAll('.theme-icon');
        for (var i = 0; i < iconEls.length; i++) {
            iconEls[i].textContent = icon;
        }
    }

    function applyTheme(mode) {
        var d = document.documentElement;
        var effectiveTheme = mode;
        if (mode === 'system') {
            effectiveTheme = isSystemDark() ? 'dark' : 'light';
        }
        d.setAttribute('data-theme', effectiveTheme);
        d.setAttribute('data-theme-mode', mode);
        updateThemeUI(mode);
    }

    // 1. Initial application on script load
    var initialMode = getStoredTheme();
    applyTheme(initialMode);

    // 2. Global Toggle Function
    window.jtracToggleTheme = function(e) {
        if (e) {
            if (e.preventDefault) e.preventDefault();
            if (e.stopPropagation) e.stopPropagation();
        }
        var d = document.documentElement;
        var currentMode = d.getAttribute('data-theme-mode') || 'system';
        var nextMode;
        if (currentMode === 'light') {
            nextMode = 'dark';
        } else if (currentMode === 'dark') {
            nextMode = 'system';
        } else {
            nextMode = 'light';
        }

        try {
            localStorage.setItem('jtrac-theme', nextMode);
        } catch(err) {
            // Gracefully handle private browsing / quota errors
        }

        applyTheme(nextMode);
        return false;
    };

    // 3. React to OS system dark mode changes when mode is 'system'
    if (window.matchMedia) {
        try {
            window.matchMedia('(prefers-color-scheme: dark)').addEventListener('change', function(e) {
                var d = document.documentElement;
                if (d.getAttribute('data-theme-mode') === 'system') {
                    d.setAttribute('data-theme', e.matches ? 'dark' : 'light');
                }
            });
        } catch(e) {}
    }

    // 4. Ensure icons are synced when DOM is ready
    function syncOnReady() {
        var currentMode = document.documentElement.getAttribute('data-theme-mode') || getStoredTheme();
        updateThemeUI(currentMode);

        // Bind touch/click listeners to buttons as fallback
        var btns = document.querySelectorAll('.mobile-theme-btn, .desktop-theme-btn, .login-theme-btn, .nav-tab-theme');
        for (var i = 0; i < btns.length; i++) {
            btns[i].onclick = window.jtracToggleTheme;
        }
    }

    if (document.readyState === 'loading') {
        document.addEventListener('DOMContentLoaded', syncOnReady);
    } else {
        syncOnReady();
    }
})();
