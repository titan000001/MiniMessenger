// Dynamic Configuration
window.applyMiniMessengerConfig = function(config) {
    // Default config if none provided
    config = config || { darkMode: true, hideClutter: true };

    const cssParts = [];

    // --- DARK MODE ---
    if (config.darkMode) {
        cssParts.push(`
            /* Dark Mode Variables (Simulated) */
            :root {
                --dark-bg: #121212;
                --dark-surface: #1e1e1e;
                --dark-text: #e0e0e0;
                --dark-text-secondary: #a0a0a0;
                --primary-color: #0084ff;
            }

            /* Force Dark Background on Body and Main Containers */
            body, html, #viewport, ._5sch, ._4g33, ._52z5, ._52z7, .touch {
                background-color: var(--dark-bg) !important;
                color: var(--dark-text) !important;
            }

            /* Message List / Threads */
            ._55wp, ._2v6p {
                background-color: var(--dark-bg) !important;
            }

            /* Text Colors */
            h1, h2, h3, h4, span, div, p, a {
                color: var(--dark-text) !important;
            }

            /* Input Fields */
            input, textarea {
                background-color: var(--dark-surface) !important;
                color: var(--dark-text) !important;
                border: 1px solid #333 !important;
            }

            /* Specific Facebook M-Site Dark Mode adjustments */
            ._1qw {
                 background-color: var(--dark-surface) !important;
            }

            /* Navigation Bar */
            ._4g34, ._59v1 {
                background-color: var(--dark-surface) !important;
                border-bottom: 1px solid #333 !important;
            }

            /* Reset Link Colors inside messages to be readable */
            ._52mr a {
                color: #448AFF !important;
            }
        `);
    }

    // --- CLUTTER REMOVAL ---
    if (config.hideClutter) {
        cssParts.push(`
            /* Hide "Install App" banners */
            [data-sigil="m-b-u-c-s"], ._5spx, ._5s-r, [data-sigil="header-cta"] {
                display: none !important;
            }

            /* Hide Stories */
            [data-sigil="story-tray"], ._57-w, ._57-v {
                display: none !important;
            }

            /* Hide "People You May Know" or suggestions */
            [id^="u_0_"], ._585- {
                /* Generic selector, requires caution */
            }
        `);
    }

    // Combine CSS
    const finalCSS = cssParts.join('\n');

    // Apply Styles
    const styleId = 'mini-messenger-style';
    let style = document.getElementById(styleId);
    if (!style) {
        style = document.createElement('style');
        style.id = styleId;
        style.type = 'text/css';
        document.head.appendChild(style);
    }
    style.textContent = finalCSS;
};

// Observer to re-apply if lost (Single Page App navigation)
// We only set this up once
if (!window.miniMessengerObserver) {
    window.miniMessengerObserver = new MutationObserver(function(mutations) {
        const style = document.getElementById('mini-messenger-style');
        // If style tag is gone, we might need to re-run.
        // But since we store the config in closure, we'd need to re-call apply.
        // For simplicity, we assume the style tag stays unless the head is wiped.
        // If the head is wiped, the MainActivity will likely re-inject.
    });
    // window.miniMessengerObserver.observe(document.body, { childList: true, subtree: true });
}
