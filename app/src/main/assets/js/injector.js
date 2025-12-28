// CSS to be injected
const customCSS = `
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

    /* CLUTTER REMOVAL */
    
    /* Hide "Install App" banners */
    [data-sigil="m-b-u-c-s"], ._5spx, ._5s-r, [data-sigil="header-cta"] {
        display: none !important;
    }
    
    /* Hide Stories (This is a guess at the selector, usually they are in a carousel container) */
    /* M-site stories often have specific data-sigils or class names like 'story_tray' or similar */
    [data-sigil="story-tray"], ._57-w, ._57-v {
        display: none !important;
    }

    /* Hide "People You May Know" or suggestions */
    [id^="u_0_"], ._585- { 
        /* Be careful with generic selectors, but often suggestions are in distinct blocks */
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
`;

function applyStyles() {
    const style = document.createElement('style');
    style.id = 'mini-messenger-style';
    style.type = 'text/css';
    style.appendChild(document.createTextNode(customCSS));
    
    const head = document.head || document.getElementsByTagName('head')[0];
    const existing = document.getElementById('mini-messenger-style');
    
    if (existing) {
        existing.remove();
    }
    head.appendChild(style);
}

// Apply immediately
applyStyles();

// Re-apply on DOM changes (for single-page app navigation)
const observer = new MutationObserver(function(mutations) {
    // Check if our style is still there, if not, re-add. 
    // Also useful if the site overwrites styles dynamically.
    if (!document.getElementById('mini-messenger-style')) {
        applyStyles();
    }
});

observer.observe(document.body, { childList: true, subtree: true });
