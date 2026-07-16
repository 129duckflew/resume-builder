INSERT INTO themes (id, name, description, css_content, is_built_in, sort_order, variables_schema, layout, user_id)
VALUES (
    'classic',
    'Classic',
    'Traditional corporate style with serif fonts, monochrome palette, uppercase headings',
    $$/* ===================================================
   Classic Theme — Traditional Corporate & Academic Style
   Serif / Monochrome / Uppercase headings / Justified
   =================================================== */

@page {
    size: A4;
    margin: var(--page-margin, 20mm 25mm);
    background-color: var(--background-color, #ffffff);
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family, 'Times New Roman', Times, serif);
    color: var(--text-color, #000000);
    font-size: var(--font-size, 11pt);
    line-height: var(--line-height, 1.4);
    background: var(--background-color, #ffffff);
}

.resume-page {
    width: 100%;
    margin: 0 auto;
    padding: 6mm 10mm;
}

/* ----- Header ----- */
header, .resume-header {
    text-align: center;
    margin-bottom: 18px;
}

h1 {
    font-size: var(--heading-font-size, 20pt);
    font-weight: bold;
    text-transform: uppercase;
    margin-bottom: 6px;
    letter-spacing: 0.5px;
}

.contact-info, .resume-contact {
    font-size: 10.5pt;
    margin-bottom: 3px;
}

/* Center contact info/role paragraphs immediately after the name heading */
h1 + p, h1 + p + p {
    text-align: center;
    margin-bottom: 2px;
}

/* ----- Sections ----- */
.section, .resume-section {
    margin-bottom: var(--section-spacing, 16px);
}

h2 {
    font-size: var(--section-heading-size, 12pt);
    font-weight: bold;
    text-transform: uppercase;
    border-bottom: 1px solid var(--primary-color, #000000);
    padding-bottom: 2px;
    margin-bottom: 8px;
    letter-spacing: 0.5px;
}

h3 {
    font-size: var(--font-size, 11pt);
    font-weight: bold;
    margin-bottom: 4px;
}

p {
    margin-bottom: 6px;
    text-align: justify;
}

strong {
    font-weight: bold;
}

em {
    font-family: 'Georgia', 'Palatino Linotype', 'Book Antiqua', Palatino, 'Times New Roman', Times, serif;
    font-style: italic;
    letter-spacing: 0.2px;
}

/* ----- Items (Experience / Education) ----- */
.item-title, .resume-item-title {
    font-weight: bold;
}

.item-date, .resume-item-date {
    float: right;
}

.item-subtitle, .resume-item-subtitle {
    font-style: italic;
    margin-bottom: 5px;
}

.clear, .clearfix {
    clear: both;
}

/* ----- Lists ----- */
ul {
    list-style-type: disc;
    margin-left: 18px;
    margin-bottom: 8px;
}

li {
    margin-bottom: 4px;
    text-align: justify;
}

.project-block, .resume-project {
    margin-bottom: 12px;
}

.bold-text, .resume-bold {
    font-weight: bold;
}

/* ----- Links ----- */
a {
    color: var(--primary-color, #000000);
    text-decoration: none;
}

a:hover {
    text-decoration: underline;
}

/* ----- Print media query ----- */
@media print {
    body {
        background: var(--background-color, #ffffff);
    }
    .resume-page {
        padding: 0;
        max-width: none;
    }
}$$,
    true,
    1,
    '[{"name":"--primary-color","type":"color","default":"#000000","label":"Primary Color","group":"Colors"},{"name":"--text-color","type":"color","default":"#000000","label":"Text Color","group":"Colors"},{"name":"--heading-color","type":"color","default":"#000000","label":"Heading Color","group":"Colors"},{"name":"--background-color","type":"color","default":"#ffffff","label":"Background","group":"Colors"},{"name":"--font-family","type":"font","default":"''Times New Roman'', Times, serif","label":"Font Family","group":"Typography"},{"name":"--font-size","type":"size","default":"11pt","label":"Base Font Size","group":"Typography"},{"name":"--heading-font-size","type":"size","default":"20pt","label":"Name/Title Size","group":"Typography"},{"name":"--section-heading-size","type":"size","default":"12pt","label":"Section Heading Size","group":"Typography"},{"name":"--line-height","type":"size","default":"1.4","label":"Line Height","group":"Typography"},{"name":"--page-margin","type":"size","default":"20mm 25mm","label":"Page Margin","group":"Layout"},{"name":"--section-spacing","type":"size","default":"16px","label":"Section Spacing","group":"Layout"}]',
    'single',
    NULL
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO themes (id, name, description, css_content, is_built_in, sort_order, variables_schema, layout, user_id)
VALUES (
    'modern',
    'Modern',
    'Clean sans-serif style with subtle blue accents, suitable for tech companies',
    $$/* ===================================================
   Modern Theme — Clean Sans-Serif / Blue Accent
   =================================================== */

@page {
    size: A4;
    margin: var(--page-margin, 18mm 22mm);
    background-color: var(--background-color, #ffffff);
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family, 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif);
    color: var(--text-color, #1a1a1a);
    font-size: var(--font-size, 10.5pt);
    line-height: var(--line-height, 1.5);
    background: var(--background-color, #ffffff);
}

.resume-page {
    width: 100%;
    margin: 0 auto;
    padding: 6mm 10mm;
}

/* ----- Header ----- */
.resume-header {
    margin-bottom: 20px;
    padding-bottom: 14px;
    border-bottom: 2px solid var(--primary-color, #2563eb);
}

h1 {
    font-size: var(--heading-font-size, 22pt);
    font-weight: 700;
    color: var(--heading-color, #111827);
    margin-bottom: 4px;
    letter-spacing: -0.3px;
}

.contact-info, .resume-contact {
    font-size: 10pt;
    color: var(--text-color, #4b5563);
    margin-bottom: 2px;
}

.resume-contact a {
    color: var(--primary-color, #2563eb);
}

/* ----- Sections ----- */
.resume-section {
    margin-bottom: var(--section-spacing, 14px);
}

h2 {
    font-size: var(--section-heading-size, 11pt);
    font-weight: 600;
    color: var(--primary-color, #2563eb);
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 1px solid #e5e7eb;
    padding-bottom: 3px;
    margin-bottom: 8px;
}

h3 {
    font-size: 10.5pt;
    font-weight: 600;
    color: var(--heading-color, #111827);
    margin-bottom: 4px;
}

p {
    margin-bottom: 6px;
    color: var(--text-color, #374151);
    text-align: justify;
}

strong {
    color: var(--heading-color, #111827);
}

em {
    color: var(--text-color, #374151);
}

/* ----- Items ----- */
.resume-item-title {
    font-weight: 600;
    color: var(--heading-color, #111827);
}

.resume-item-date {
    float: right;
    color: var(--text-color, #6b7280);
    font-size: 9.5pt;
}

.resume-item-subtitle {
    color: var(--text-color, #374151);
    margin-bottom: 4px;
}

.clearfix {
    clear: both;
}

/* ----- Lists ----- */
ul {
    list-style-type: none;
    margin-left: 0;
    margin-bottom: 6px;
}

li {
    margin-bottom: 3px;
    padding-left: 14px;
    position: relative;
    color: var(--text-color, #374151);
}

li::before {
    content: "\2014";
    position: absolute;
    left: 0;
    color: var(--primary-color, #2563eb);
}

.project-block, .resume-project {
    margin-bottom: 10px;
}

/* ----- Links ----- */
a {
    color: var(--primary-color, #2563eb);
    text-decoration: none;
}

a:hover {
    text-decoration: underline;
}

@media print {
    body { background: var(--background-color, #ffffff); }
    .resume-page { padding: 0; max-width: none; }
}$$,
    true,
    2,
    '[{"name":"--primary-color","type":"color","default":"#2563eb","label":"Primary Color","group":"Colors"},{"name":"--text-color","type":"color","default":"#1a1a1a","label":"Text Color","group":"Colors"},{"name":"--heading-color","type":"color","default":"#111827","label":"Heading Color","group":"Colors"},{"name":"--background-color","type":"color","default":"#ffffff","label":"Background","group":"Colors"},{"name":"--font-family","type":"font","default":"''Inter'', ''Helvetica Neue'', Helvetica, Arial, sans-serif","label":"Font Family","group":"Typography"},{"name":"--font-size","type":"size","default":"10.5pt","label":"Base Font Size","group":"Typography"},{"name":"--heading-font-size","type":"size","default":"22pt","label":"Name/Title Size","group":"Typography"},{"name":"--section-heading-size","type":"size","default":"11pt","label":"Section Heading Size","group":"Typography"},{"name":"--line-height","type":"size","default":"1.5","label":"Line Height","group":"Typography"},{"name":"--page-margin","type":"size","default":"18mm 22mm","label":"Page Margin","group":"Layout"},{"name":"--section-spacing","type":"size","default":"14px","label":"Section Spacing","group":"Layout"}]',
    'single',
    NULL
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO themes (id, name, description, css_content, is_built_in, sort_order, variables_schema, layout, user_id)
VALUES (
    'minimal',
    'Minimal',
    'Ultra-minimalist style with maximum whitespace, suitable for academic/research positions',
    $$/* ===================================================
   Minimal Theme — Ultra-Minimalist / Maximum Whitespace
   =================================================== */

@page {
    size: A4;
    margin: var(--page-margin, 22mm 28mm);
    background-color: var(--background-color, #ffffff);
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family, system-ui, -apple-system, 'Segoe UI', Roboto, sans-serif);
    color: var(--text-color, #222222);
    font-size: var(--font-size, 10pt);
    line-height: var(--line-height, 1.6);
    background: var(--background-color, #ffffff);
}

.resume-page {
    width: 100%;
    margin: 0 auto;
    padding: 6mm 10mm;
}

/* ----- Header ----- */
.resume-header {
    text-align: left;
    margin-bottom: 24px;
}

h1 {
    font-size: var(--heading-font-size, 18pt);
    font-weight: 300;
    color: var(--heading-color, #000000);
    margin-bottom: 4px;
    letter-spacing: 2px;
    text-transform: uppercase;
}

.contact-info, .resume-contact {
    font-size: 9.5pt;
    color: var(--muted-color, #666666);
    margin-bottom: 2px;
}

/* ----- Sections ----- */
.resume-section {
    margin-bottom: var(--section-spacing, 20px);
}

h2 {
    font-size: var(--section-heading-size, 9pt);
    font-weight: 400;
    color: var(--muted-color, #999999);
    text-transform: uppercase;
    letter-spacing: 2px;
    margin-bottom: 10px;
    padding-bottom: 0;
    border-bottom: none;
}

h3 {
    font-size: var(--font-size, 10pt);
    font-weight: 500;
    color: var(--heading-color, #000000);
    margin-bottom: 4px;
}

p {
    margin-bottom: 6px;
    color: var(--text-color, #444444);
    text-align: justify;
}

strong {
    color: var(--heading-color, #000000);
}

em {
    color: var(--muted-color, #666666);
}

/* ----- Items ----- */
.resume-item-title {
    font-weight: 500;
    color: var(--heading-color, #000000);
}

.resume-item-date {
    float: right;
    color: var(--muted-color, #999999);
    font-size: 9pt;
}

.resume-item-subtitle {
    color: var(--muted-color, #666666);
    font-size: 9.5pt;
    margin-bottom: 4px;
}

.clearfix {
    clear: both;
}

/* ----- Lists ----- */
ul {
    list-style-type: none;
    margin-left: 0;
    margin-bottom: 8px;
}

li {
    margin-bottom: 3px;
    color: var(--text-color, #444444);
}

.project-block, .resume-project {
    margin-bottom: 12px;
}

/* ----- Links ----- */
a {
    color: var(--primary-color, #222222);
    text-decoration: none;
    border-bottom: 0.5px solid var(--muted-color, #cccccc);
}

a:hover {
    border-bottom-color: var(--primary-color, #222222);
}

@media print {
    body { background: var(--background-color, #ffffff); }
    .resume-page { padding: 0; max-width: none; }
}$$,
    true,
    3,
    '[{"name":"--primary-color","type":"color","default":"#222222","label":"Primary Color","group":"Colors"},{"name":"--text-color","type":"color","default":"#222222","label":"Text Color","group":"Colors"},{"name":"--heading-color","type":"color","default":"#000000","label":"Heading Color","group":"Colors"},{"name":"--muted-color","type":"color","default":"#999999","label":"Muted Color","group":"Colors"},{"name":"--background-color","type":"color","default":"#ffffff","label":"Background","group":"Colors"},{"name":"--font-family","type":"font","default":"system-ui, -apple-system, ''Segoe UI'', Roboto, sans-serif","label":"Font Family","group":"Typography"},{"name":"--font-size","type":"size","default":"10pt","label":"Base Font Size","group":"Typography"},{"name":"--heading-font-size","type":"size","default":"18pt","label":"Name/Title Size","group":"Typography"},{"name":"--section-heading-size","type":"size","default":"9pt","label":"Section Heading Size","group":"Typography"},{"name":"--line-height","type":"size","default":"1.6","label":"Line Height","group":"Typography"},{"name":"--page-margin","type":"size","default":"22mm 28mm","label":"Page Margin","group":"Layout"},{"name":"--section-spacing","type":"size","default":"20px","label":"Section Spacing","group":"Layout"}]',
    'single',
    NULL
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO themes (id, name, description, css_content, is_built_in, sort_order, variables_schema, layout, user_id)
VALUES (
    'sidebar',
    'Sidebar',
    'Two-column layout with a colored sidebar for contact and skills',
    $$/* ===================================================
   Sidebar Theme — Two-Column with Colored Sidebar
   =================================================== */

@page {
    size: A4;
    margin: 0;
    background-color: var(--background-color, #f8f9fa);
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family, 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif);
    color: var(--text-color, #1a202c);
    font-size: var(--font-size, 10.5pt);
    line-height: var(--line-height, 1.5);
    background: var(--background-color, #f8f9fa);
}

.resume-page {
    display: flex;
    min-height: 297mm;
    width: 210mm;
    margin: 0 auto;
}

/* ----- Sidebar (left column) ----- */
.resume-sidebar {
    width: var(--sidebar-width, 35%);
    background: var(--sidebar-bg, #1a365d);
    color: var(--sidebar-text, #e2e8f0);
    padding: 10mm 8mm;
    flex-shrink: 0;
}

.resume-sidebar h1 {
    font-size: var(--heading-font-size, 16pt);
    font-weight: 700;
    color: #ffffff;
    margin-bottom: 4px;
    letter-spacing: 0.5px;
}

.resume-sidebar .contact-info,
.resume-sidebar .resume-contact {
    font-size: 9pt;
    color: var(--sidebar-text, #a0aec0);
    margin-bottom: 2px;
}

.resume-sidebar h2 {
    font-size: 9pt;
    font-weight: 600;
    color: var(--sidebar-heading, #90cdf4);
    text-transform: uppercase;
    letter-spacing: 1.5px;
    border-bottom: 1px solid #2d4a7a;
    padding-bottom: 3px;
    margin: 14px 0 6px;
}

.resume-sidebar p {
    color: var(--sidebar-text, #cbd5e0);
    font-size: 9pt;
    margin-bottom: 4px;
}

.resume-sidebar ul {
    list-style: none;
    margin-left: 0;
    margin-bottom: 6px;
}

.resume-sidebar li {
    color: var(--sidebar-text, #cbd5e0);
    font-size: 9pt;
    margin-bottom: 2px;
    padding-left: 0;
}

.resume-sidebar a {
    color: var(--sidebar-heading, #90cdf4);
    text-decoration: none;
}

.resume-sidebar a:hover {
    text-decoration: underline;
}

.resume-sidebar strong {
    color: #ffffff;
}

/* ----- Main content (right column) ----- */
.resume-main {
    width: calc(100% - var(--sidebar-width, 35%));
    padding: 10mm 9mm;
    background: #ffffff;
}

.resume-main h1 {
    font-size: var(--main-heading-font-size, 18pt);
    font-weight: 700;
    color: var(--heading-color, #1a365d);
    margin-bottom: 4px;
    letter-spacing: -0.3px;
}

.resume-main .contact-info,
.resume-main .resume-contact {
    font-size: 9.5pt;
    color: var(--text-color, #4a5568);
    margin-bottom: 3px;
}

.resume-main a {
    color: var(--primary-color, #2b6cb0);
    text-decoration: none;
}

.resume-main h2 {
    font-size: var(--section-heading-size, 10.5pt);
    font-weight: 600;
    color: var(--heading-color, #1a365d);
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 1.5px solid var(--heading-color, #1a365d);
    padding-bottom: 2px;
    margin-bottom: 6px;
    margin-top: 10px;
}

.resume-main h3 {
    font-size: 10pt;
    font-weight: 600;
    color: var(--text-color, #2d3748);
    margin-bottom: 3px;
}

.resume-main p {
    color: var(--text-color, #4a5568);
    font-size: 9.5pt;
    margin-bottom: 4px;
    text-align: justify;
}

.resume-main ul {
    list-style-type: disc;
    margin-left: 16px;
    margin-bottom: 6px;
}

.resume-main li {
    color: var(--text-color, #4a5568);
    font-size: 9.5pt;
    margin-bottom: 2px;
    text-align: justify;
}

.resume-main strong {
    color: var(--text-color, #2d3748);
}

.resume-main em {
    color: var(--text-color, #718096);
}

/* ----- Tear-off edge between columns ----- */
.resume-page > .resume-sidebar + .resume-main {
    border-left: none;
}

/* When no sidebar sections are present, main content takes full width */
.resume-page > .resume-main:only-child {
    width: 100%;
}

@media print {
    body { background: var(--background-color, #ffffff); }
    .resume-page { max-width: none; }
}$$,
    true,
    4,
    '[{"name":"--primary-color","type":"color","default":"#1a365d","label":"Primary Color","group":"Colors"},{"name":"--sidebar-bg","type":"color","default":"#1a365d","label":"Sidebar Background","group":"Colors"},{"name":"--sidebar-text","type":"color","default":"#e2e8f0","label":"Sidebar Text","group":"Colors"},{"name":"--sidebar-heading","type":"color","default":"#90cdf4","label":"Sidebar Heading","group":"Colors"},{"name":"--text-color","type":"color","default":"#1a202c","label":"Text Color","group":"Colors"},{"name":"--heading-color","type":"color","default":"#1a365d","label":"Heading Color","group":"Colors"},{"name":"--background-color","type":"color","default":"#f8f9fa","label":"Background","group":"Colors"},{"name":"--sidebar-width","type":"size","default":"35%","label":"Sidebar Width","group":"Layout"},{"name":"--font-family","type":"font","default":"''Inter'', ''Helvetica Neue'', Helvetica, Arial, sans-serif","label":"Font Family","group":"Typography"},{"name":"--font-size","type":"size","default":"10.5pt","label":"Base Font Size","group":"Typography"},{"name":"--heading-font-size","type":"size","default":"16pt","label":"Sidebar Name Size","group":"Typography"},{"name":"--main-heading-font-size","type":"size","default":"18pt","label":"Main Content Name Size","group":"Typography"},{"name":"--section-heading-size","type":"size","default":"10.5pt","label":"Section Heading Size","group":"Typography"},{"name":"--line-height","type":"size","default":"1.5","label":"Line Height","group":"Typography"},{"name":"--section-spacing","type":"size","default":"12px","label":"Section Spacing","group":"Layout"}]',
    'sidebar-left',
    NULL
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO themes (id, name, description, css_content, is_built_in, sort_order, variables_schema, layout, user_id)
VALUES (
    'stackoverflow',
    'Stack Overflow',
    'Developer-friendly style inspired by Stack Overflow, with tag-like skills',
    $$/* ===================================================
   Stack Overflow Theme — Developer Community Style
   =================================================== */

@page {
    size: A4;
    margin: var(--page-margin, 15mm 18mm);
    background-color: var(--background-color, #ffffff);
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family, 'Inter', 'Segoe UI', 'Helvetica Neue', Arial, sans-serif);
    color: var(--text-color, #2f3337);
    font-size: var(--font-size, 10.5pt);
    line-height: var(--line-height, 1.5);
    background: var(--background-color, #ffffff);
}

.resume-page {
    width: 100%;
    margin: 0 auto;
    padding: 0;
}

/* ----- Header ----- */
h1 {
    font-size: var(--heading-font-size, 20pt);
    font-weight: 700;
    color: var(--heading-color, #2f3337);
    margin-bottom: 2px;
}

.contact-info, .resume-contact {
    font-size: 9.5pt;
    color: var(--muted-color, #6a737c);
    margin-bottom: 2px;
}

.resume-contact a {
    color: var(--link-color, #0077cc);
}

/* ----- Summary / intro ----- */
h1 + p {
    margin-top: 8px;
    padding: 8px 12px;
    background: #f9f9f9;
    border-left: 3px solid var(--primary-color, #f48024);
    border-radius: 3px;
    color: var(--text-color, #3c4146);
    font-size: 10pt;
}

/* ----- Sections ----- */
.resume-section {
    margin-bottom: var(--section-spacing, 10px);
}

h2 {
    font-size: var(--section-heading-size, 11pt);
    font-weight: 600;
    color: var(--primary-color, #f48024);
    text-transform: uppercase;
    letter-spacing: 0.8px;
    border-bottom: 1px solid #e4e6e8;
    padding-bottom: 3px;
    margin: 10px 0 6px;
}

h3 {
    font-size: var(--font-size, 10.5pt);
    font-weight: 600;
    color: var(--heading-color, #2f3337);
    margin-bottom: 2px;
}

p {
    color: var(--text-color, #3c4146);
    font-size: 10pt;
    margin-bottom: 4px;
    text-align: justify;
}

/* ----- Items ----- */
.resume-item-title {
    font-weight: 600;
    color: var(--heading-color, #2f3337);
}

.resume-item-date {
    float: right;
    color: var(--muted-color, #6a737c);
    font-size: 9pt;
}

.resume-item-subtitle {
    color: var(--muted-color, #6a737c);
    font-size: 9.5pt;
    font-style: italic;
    margin-bottom: 3px;
}

.clearfix {
    clear: both;
}

/* ----- Lists (as bullet points) ----- */
ul {
    list-style-type: none;
    margin-left: 0;
    margin-bottom: 4px;
}

li {
    margin-bottom: 2px;
    padding-left: 14px;
    position: relative;
    color: var(--text-color, #3c4146);
    font-size: 10pt;
}

li::before {
    content: "\2022";
    position: absolute;
    left: 0;
    color: var(--primary-color, #f48024);
    font-weight: bold;
}

/* ----- Tag-like skills ----- */
.skills-tags, .resume-skills {
    display: flex;
    flex-wrap: wrap;
    gap: 4px;
    margin-bottom: 6px;
}

.skills-tags li, .resume-skills li {
    background: var(--tag-bg, #e4e6e8);
    color: var(--heading-color, #2f3337);
    padding: 2px 6px;
    border-radius: 3px;
    font-size: 9pt;
    list-style: none;
    margin-bottom: 0;
}

.skills-tags li::before, .resume-skills li::before {
    content: none;
}

strong {
    color: var(--heading-color, #2f3337);
}

em {
    color: var(--muted-color, #6a737c);
}

/* ----- Links ----- */
a {
    color: var(--link-color, #0077cc);
    text-decoration: none;
}

a:hover {
    color: var(--primary-color, #f48024);
}

@media print {
    body { background: var(--background-color, #ffffff); }
}$$,
    true,
    5,
    '[{"name":"--primary-color","type":"color","default":"#f48024","label":"Primary Color","group":"Colors"},{"name":"--link-color","type":"color","default":"#0077cc","label":"Link Color","group":"Colors"},{"name":"--text-color","type":"color","default":"#2f3337","label":"Text Color","group":"Colors"},{"name":"--heading-color","type":"color","default":"#2f3337","label":"Heading Color","group":"Colors"},{"name":"--muted-color","type":"color","default":"#6a737c","label":"Muted Color","group":"Colors"},{"name":"--tag-bg","type":"color","default":"#e4e6e8","label":"Tag Background","group":"Colors"},{"name":"--background-color","type":"color","default":"#ffffff","label":"Background","group":"Colors"},{"name":"--font-family","type":"font","default":"''Inter'', ''Segoe UI'', ''Helvetica Neue'', Arial, sans-serif","label":"Font Family","group":"Typography"},{"name":"--font-size","type":"size","default":"10.5pt","label":"Base Font Size","group":"Typography"},{"name":"--heading-font-size","type":"size","default":"20pt","label":"Name/Title Size","group":"Typography"},{"name":"--section-heading-size","type":"size","default":"11pt","label":"Section Heading Size","group":"Typography"},{"name":"--line-height","type":"size","default":"1.5","label":"Line Height","group":"Typography"},{"name":"--page-margin","type":"size","default":"15mm 18mm","label":"Page Margin","group":"Layout"},{"name":"--section-spacing","type":"size","default":"10px","label":"Section Spacing","group":"Layout"}]',
    'single',
    NULL
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO themes (id, name, description, css_content, is_built_in, sort_order, variables_schema, layout, user_id)
VALUES (
    'elegant',
    'Elegant',
    'Refined business style with warm tones and delicate serif typography',
    $$/* ===================================================
   Elegant Theme — Refined Business / Serif Print
   =================================================== */

@page {
    size: A4;
    margin: var(--page-margin, 18mm 22mm);
    background-color: var(--background-color, #faf9f6);
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family, 'Georgia', 'Palatino Linotype', 'Book Antiqua', Palatino, serif);
    color: var(--text-color, #2d2a24);
    font-size: var(--font-size, 10.5pt);
    line-height: var(--line-height, 1.55);
    background: var(--background-color, #faf9f6);
}

.resume-page {
    width: 100%;
    margin: 0 auto;
    padding: 0;
}

/* ----- Header ----- */
h1 {
    font-size: var(--heading-font-size, 22pt);
    font-weight: 400;
    color: var(--heading-color, #1b4332);
    margin-bottom: 2px;
    letter-spacing: 2px;
    text-transform: uppercase;
    border-bottom: 0.5px solid var(--accent-color, #cbb99b);
    padding-bottom: 8px;
}

.contact-info, .resume-contact {
    font-size: 9.5pt;
    color: var(--muted-color, #6b6858);
    margin-bottom: 3px;
    margin-top: 4px;
    letter-spacing: 0.3px;
}

.resume-contact a {
    color: var(--primary-color, #1b4332);
    text-decoration: none;
    border-bottom: 0.5px solid var(--accent-color, #cbb99b);
}

/* ----- Sections ----- */
.resume-section {
    margin-bottom: var(--section-spacing, 12px);
}

h2 {
    font-size: var(--section-heading-size, 10pt);
    font-weight: 600;
    color: var(--heading-color, #1b4332);
    text-transform: uppercase;
    letter-spacing: 2px;
    margin: 12px 0 6px;
    padding-bottom: 2px;
    border-bottom: 0.5px solid var(--accent-color, #cbb99b);
}

h3 {
    font-size: var(--font-size, 10.5pt);
    font-weight: 600;
    color: var(--text-color, #2d2a24);
    font-style: italic;
    margin-bottom: 2px;
}

p {
    color: var(--text-color, #3d3a33);
    font-size: 10pt;
    margin-bottom: 5px;
    text-align: justify;
}

/* ----- Items ----- */
.resume-item-title {
    font-weight: 600;
    color: var(--heading-color, #1b4332);
}

.resume-item-date {
    float: right;
    color: var(--muted-color, #8b8878);
    font-size: 9pt;
    font-style: italic;
}

.resume-item-subtitle {
    color: var(--muted-color, #6b6858);
    font-size: 9.5pt;
    font-style: italic;
    margin-bottom: 3px;
}

.clearfix {
    clear: both;
}

/* ----- Lists ----- */
ul {
    list-style-type: none;
    margin-left: 0;
    margin-bottom: 6px;
}

li {
    margin-bottom: 3px;
    padding-left: 18px;
    position: relative;
    color: var(--text-color, #3d3a33);
    font-size: 10pt;
    text-align: justify;
}

li::before {
    content: "\2014";
    position: absolute;
    left: 0;
    color: var(--accent-color, #cbb99b);
}

strong {
    color: var(--heading-color, #1b4332);
    font-weight: 600;
}

em {
    color: var(--muted-color, #6b6858);
}

/* ----- Links ----- */
a {
    color: var(--primary-color, #1b4332);
    text-decoration: none;
    border-bottom: 0.5px solid transparent;
    transition: border-color 0.2s;
}

a:hover {
    border-bottom-color: var(--primary-color, #1b4332);
}

@media print {
    body { background: #ffffff; }
}$$,
    true,
    6,
    '[{"name":"--primary-color","type":"color","default":"#1b4332","label":"Primary Color","group":"Colors"},{"name":"--text-color","type":"color","default":"#2d2a24","label":"Text Color","group":"Colors"},{"name":"--heading-color","type":"color","default":"#1b4332","label":"Heading Color","group":"Colors"},{"name":"--muted-color","type":"color","default":"#6b6858","label":"Muted Color","group":"Colors"},{"name":"--accent-color","type":"color","default":"#cbb99b","label":"Accent Border","group":"Colors"},{"name":"--background-color","type":"color","default":"#faf9f6","label":"Background","group":"Colors"},{"name":"--font-family","type":"font","default":"''Georgia'', ''Palatino Linotype'', ''Book Antiqua'', Palatino, serif","label":"Font Family","group":"Typography"},{"name":"--font-size","type":"size","default":"10.5pt","label":"Base Font Size","group":"Typography"},{"name":"--heading-font-size","type":"size","default":"22pt","label":"Name/Title Size","group":"Typography"},{"name":"--section-heading-size","type":"size","default":"10pt","label":"Section Heading Size","group":"Typography"},{"name":"--line-height","type":"size","default":"1.55","label":"Line Height","group":"Typography"},{"name":"--page-margin","type":"size","default":"18mm 22mm","label":"Page Margin","group":"Layout"},{"name":"--section-spacing","type":"size","default":"12px","label":"Section Spacing","group":"Layout"}]',
    'single',
    NULL
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO themes (id, name, description, css_content, is_built_in, sort_order, variables_schema, layout, user_id)
VALUES (
    'compact',
    'Compact',
    'Dense layout for experienced professionals — maximum content per page',
    $$/* ===================================================
   Compact Theme — Dense / High Information Density
   =================================================== */

@page {
    size: A4;
    margin: var(--page-margin, 12mm 16mm);
    background-color: var(--background-color, #ffffff);
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family, 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif);
    color: var(--text-color, #1a1a1a);
    font-size: var(--font-size, 9.5pt);
    line-height: var(--line-height, 1.35);
    background: var(--background-color, #ffffff);
}

.resume-page {
    width: 100%;
    margin: 0 auto;
    padding: 0;
}

/* ----- Header ----- */
h1 {
    font-size: var(--heading-font-size, 16pt);
    font-weight: 700;
    color: var(--heading-color, #000000);
    margin-bottom: 1px;
    letter-spacing: -0.3px;
}

.contact-info, .resume-contact {
    font-size: 8.5pt;
    color: var(--muted-color, #555555);
    margin-bottom: 1px;
}

.resume-contact a {
    color: var(--primary-color, #1a1a1a);
}

/* ----- Sections ----- */
.resume-section {
    margin-bottom: var(--section-spacing, 6px);
}

h2 {
    font-size: var(--section-heading-size, 9pt);
    font-weight: 600;
    color: var(--heading-color, #000000);
    text-transform: uppercase;
    letter-spacing: 0.5px;
    border-bottom: 0.5px solid #cccccc;
    padding-bottom: 1px;
    margin-bottom: 3px;
    margin-top: 6px;
}

h3 {
    font-size: var(--font-size, 9pt);
    font-weight: 600;
    color: var(--text-color, #1a1a1a);
    margin-bottom: 1px;
}

p {
    color: var(--text-color, #333333);
    font-size: var(--font-size, 9pt);
    margin-bottom: 2px;
    text-align: justify;
}

/* ----- Items ----- */
.resume-item-title {
    font-weight: 600;
    color: var(--heading-color, #000000);
    font-size: var(--font-size, 9pt);
}

.resume-item-date {
    float: right;
    color: var(--muted-color, #777777);
    font-size: 8pt;
}

.resume-item-subtitle {
    color: var(--muted-color, #555555);
    font-size: 8.5pt;
    font-style: italic;
    margin-bottom: 1px;
}

.clearfix {
    clear: both;
}

/* ----- Lists ----- */
ul {
    list-style-type: none;
    margin-left: 0;
    margin-bottom: 2px;
}

li {
    margin-bottom: 1px;
    padding-left: 12px;
    position: relative;
    color: var(--text-color, #333333);
    font-size: var(--font-size, 9pt);
    text-align: justify;
}

li::before {
    content: "\2013";
    position: absolute;
    left: 0;
    color: var(--muted-color, #999999);
}

/* Nested paragraphs inside list items — compact */
li p {
    margin-bottom: 0;
    font-size: inherit;
}

strong {
    color: var(--heading-color, #000000);
}

em {
    color: var(--muted-color, #666666);
}

a {
    color: var(--primary-color, #1a1a1a);
    text-decoration: none;
}

a:hover {
    text-decoration: underline;
}

@media print {
    body { background: var(--background-color, #ffffff); }
}$$,
    true,
    7,
    '[{"name":"--primary-color","type":"color","default":"#1a1a1a","label":"Primary Color","group":"Colors"},{"name":"--text-color","type":"color","default":"#1a1a1a","label":"Text Color","group":"Colors"},{"name":"--heading-color","type":"color","default":"#000000","label":"Heading Color","group":"Colors"},{"name":"--muted-color","type":"color","default":"#555555","label":"Muted Color","group":"Colors"},{"name":"--background-color","type":"color","default":"#ffffff","label":"Background","group":"Colors"},{"name":"--font-family","type":"font","default":"''Inter'', ''Helvetica Neue'', Helvetica, Arial, sans-serif","label":"Font Family","group":"Typography"},{"name":"--font-size","type":"size","default":"9.5pt","label":"Base Font Size","group":"Typography"},{"name":"--heading-font-size","type":"size","default":"16pt","label":"Name/Title Size","group":"Typography"},{"name":"--section-heading-size","type":"size","default":"9pt","label":"Section Heading Size","group":"Typography"},{"name":"--line-height","type":"size","default":"1.35","label":"Line Height","group":"Typography"},{"name":"--page-margin","type":"size","default":"12mm 16mm","label":"Page Margin","group":"Layout"},{"name":"--section-spacing","type":"size","default":"6px","label":"Section Spacing","group":"Layout"}]',
    'single',
    NULL
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO themes (id, name, description, css_content, is_built_in, sort_order, variables_schema, layout, user_id)
VALUES (
    'sidebar-right',
    'Sidebar Right',
    'Two-column layout with colored sidebar on the right',
    $$/* ===================================================
   Sidebar Right Theme — Two-Column with Colored Sidebar on Right
   =================================================== */

@page {
    size: A4;
    margin: 0;
    background-color: var(--background-color, #f8f9fa);
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family, 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif);
    color: var(--text-color, #1a202c);
    font-size: var(--font-size, 10.5pt);
    line-height: var(--line-height, 1.5);
    background: var(--background-color, #f8f9fa);
}

.resume-page {
    display: flex;
    min-height: 297mm;
    width: 210mm;
    margin: 0 auto;
}

/* ----- Sidebar (right column) ----- */
.resume-sidebar {
    width: var(--sidebar-width, 35%);
    background: var(--sidebar-bg, #1a365d);
    color: var(--sidebar-text, #e2e8f0);
    padding: 10mm 8mm;
    flex-shrink: 0;
}

.resume-sidebar h1 {
    font-size: var(--heading-font-size, 16pt);
    font-weight: 700;
    color: #ffffff;
    margin-bottom: 4px;
    letter-spacing: 0.5px;
}

.resume-sidebar .contact-info,
.resume-sidebar .resume-contact {
    font-size: 9pt;
    color: var(--sidebar-text, #a0aec0);
    margin-bottom: 2px;
}

.resume-sidebar h2 {
    font-size: 9pt;
    font-weight: 600;
    color: var(--sidebar-heading, #90cdf4);
    text-transform: uppercase;
    letter-spacing: 1.5px;
    border-bottom: 1px solid #2d4a7a;
    padding-bottom: 3px;
    margin: 14px 0 6px;
}

.resume-sidebar p {
    color: var(--sidebar-text, #cbd5e0);
    font-size: 9pt;
    margin-bottom: 4px;
}

.resume-sidebar ul {
    list-style: none;
    margin-left: 0;
    margin-bottom: 6px;
}

.resume-sidebar li {
    color: var(--sidebar-text, #cbd5e0);
    font-size: 9pt;
    margin-bottom: 2px;
    padding-left: 0;
}

.resume-sidebar a {
    color: var(--sidebar-heading, #90cdf4);
    text-decoration: none;
}

.resume-sidebar a:hover {
    text-decoration: underline;
}

.resume-sidebar strong {
    color: #ffffff;
}

/* ----- Main content (left column) ----- */
.resume-main {
    width: calc(100% - var(--sidebar-width, 35%));
    padding: 10mm 9mm;
    background: #ffffff;
}

.resume-main h1 {
    font-size: var(--main-heading-font-size, 18pt);
    font-weight: 700;
    color: var(--heading-color, #1a365d);
    margin-bottom: 4px;
    letter-spacing: -0.3px;
}

.resume-main .contact-info,
.resume-main .resume-contact {
    font-size: 9.5pt;
    color: var(--text-color, #4a5568);
    margin-bottom: 3px;
}

.resume-main a {
    color: var(--primary-color, #2b6cb0);
    text-decoration: none;
}

.resume-main h2 {
    font-size: var(--section-heading-size, 10.5pt);
    font-weight: 600;
    color: var(--heading-color, #1a365d);
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 1.5px solid var(--heading-color, #1a365d);
    padding-bottom: 2px;
    margin-bottom: 6px;
    margin-top: 10px;
}

.resume-main h3 {
    font-size: 10pt;
    font-weight: 600;
    color: var(--text-color, #2d3748);
    margin-bottom: 3px;
}

.resume-main p {
    color: var(--text-color, #4a5568);
    font-size: 9.5pt;
    margin-bottom: 4px;
    text-align: justify;
}

.resume-main ul {
    list-style-type: disc;
    margin-left: 16px;
    margin-bottom: 6px;
}

.resume-main li {
    color: var(--text-color, #4a5568);
    font-size: 9.5pt;
    margin-bottom: 2px;
    text-align: justify;
}

.resume-main strong {
    color: var(--text-color, #2d3748);
}

.resume-main em {
    color: var(--text-color, #718096);
}

/* When no sidebar sections are present, main content takes full width */
.resume-page > .resume-main:only-child {
    width: 100%;
}

@media print {
    body { background: var(--background-color, #ffffff); }
    .resume-page { max-width: none; }
}$$,
    true,
    8,
    '[{"name":"--primary-color","type":"color","default":"#1a365d","label":"Primary Color","group":"Colors"},{"name":"--sidebar-bg","type":"color","default":"#1a365d","label":"Sidebar Background","group":"Colors"},{"name":"--sidebar-text","type":"color","default":"#e2e8f0","label":"Sidebar Text","group":"Colors"},{"name":"--sidebar-heading","type":"color","default":"#90cdf4","label":"Sidebar Heading","group":"Colors"},{"name":"--text-color","type":"color","default":"#1a202c","label":"Text Color","group":"Colors"},{"name":"--heading-color","type":"color","default":"#1a365d","label":"Heading Color","group":"Colors"},{"name":"--background-color","type":"color","default":"#f8f9fa","label":"Background","group":"Colors"},{"name":"--sidebar-width","type":"size","default":"35%","label":"Sidebar Width","group":"Layout"},{"name":"--font-family","type":"font","default":"''Inter'', ''Helvetica Neue'', Helvetica, Arial, sans-serif","label":"Font Family","group":"Typography"},{"name":"--font-size","type":"size","default":"10.5pt","label":"Base Font Size","group":"Typography"},{"name":"--heading-font-size","type":"size","default":"16pt","label":"Sidebar Name Size","group":"Typography"},{"name":"--main-heading-font-size","type":"size","default":"18pt","label":"Main Content Name Size","group":"Typography"},{"name":"--section-heading-size","type":"size","default":"10.5pt","label":"Section Heading Size","group":"Typography"},{"name":"--line-height","type":"size","default":"1.5","label":"Line Height","group":"Typography"},{"name":"--section-spacing","type":"size","default":"12px","label":"Section Spacing","group":"Layout"}]',
    'sidebar-right',
    NULL
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO themes (id, name, description, css_content, is_built_in, sort_order, variables_schema, layout, user_id)
VALUES (
    'header-bar',
    'Header Bar',
    'Top header bar with colored background and main content below',
    $$/* ===================================================
   Header Bar Theme — Top Header with Colored Background
   =================================================== */

@page {
    size: A4;
    margin: 0;
    background-color: var(--background-color, #ffffff);
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family, 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif);
    color: var(--text-color, #1a202c);
    font-size: var(--font-size, 10.5pt);
    line-height: var(--line-height, 1.5);
    background: var(--background-color, #ffffff);
}

.resume-page {
    width: 210mm;
    min-height: 297mm;
    margin: 0 auto;
    display: flex;
    flex-direction: column;
}

/* ----- Header Bar (colored background) ----- */
.resume-header-bar {
    background: var(--header-bg, #1a365d);
    color: var(--header-text, #ffffff);
    padding: 14mm 18mm 10mm;
    text-align: center;
}

.resume-header-bar h1 {
    font-size: 22pt;
    font-weight: 700;
    color: var(--header-text, #ffffff);
    margin-bottom: 4px;
    letter-spacing: 1px;
}

.resume-header-bar p {
    font-size: 9.5pt;
    color: var(--header-text, #e2e8f0);
    margin-bottom: 2px;
}

.resume-header-bar .contact-info,
.resume-header-bar .resume-contact {
    font-size: 9pt;
    color: var(--header-text, #cbd5e0);
    margin-top: 6px;
}

.resume-header-bar a {
    color: var(--header-text, #90cdf4);
    text-decoration: none;
}

.resume-header-bar a:hover {
    text-decoration: underline;
}

/* ----- Body below header ----- */
.resume-body {
    padding: 10mm 18mm;
    background: #ffffff;
    flex: 1;
}

.resume-body h2 {
    font-size: 11pt;
    font-weight: 600;
    color: var(--heading-color, #1a365d);
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 1.5px solid var(--primary-color, #2563eb);
    padding-bottom: 2px;
    margin-bottom: 6px;
    margin-top: 12px;
}

.resume-body h3 {
    font-size: 10.5pt;
    font-weight: 600;
    color: var(--text-color, #2d3748);
    margin-bottom: 3px;
}

.resume-body p {
    color: var(--text-color, #4a5568);
    font-size: 10pt;
    margin-bottom: 4px;
    text-align: justify;
}

.resume-body ul {
    list-style-type: disc;
    margin-left: 18px;
    margin-bottom: 6px;
}

.resume-body li {
    color: var(--text-color, #4a5568);
    font-size: 10pt;
    margin-bottom: 2px;
}

.resume-body strong {
    color: var(--text-color, #2d3748);
}

.resume-body em {
    color: var(--text-color, #718096);
}

.resume-body a {
    color: var(--primary-color, #2563eb);
    text-decoration: none;
}

.resume-body a:hover {
    text-decoration: underline;
}

@media print {
    body { background: var(--background-color, #ffffff); }
    .resume-page { max-width: none; }
}$$,
    true,
    9,
    '[{"name":"--header-bg","type":"color","default":"#1a365d","label":"Header Background","group":"Colors"},{"name":"--header-text","type":"color","default":"#ffffff","label":"Header Text","group":"Colors"},{"name":"--primary-color","type":"color","default":"#2563eb","label":"Primary Color","group":"Colors"},{"name":"--text-color","type":"color","default":"#1a202c","label":"Text Color","group":"Colors"},{"name":"--heading-color","type":"color","default":"#1a365d","label":"Heading Color","group":"Colors"},{"name":"--background-color","type":"color","default":"#ffffff","label":"Background","group":"Colors"},{"name":"--font-family","type":"font","default":"''Inter'', ''Helvetica Neue'', Helvetica, Arial, sans-serif","label":"Font Family","group":"Typography"},{"name":"--font-size","type":"size","default":"10.5pt","label":"Base Font Size","group":"Typography"},{"name":"--line-height","type":"size","default":"1.5","label":"Line Height","group":"Typography"},{"name":"--section-spacing","type":"size","default":"12px","label":"Section Spacing","group":"Layout"}]',
    'header-bar',
    NULL
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO themes (id, name, description, css_content, is_built_in, sort_order, variables_schema, layout, user_id)
VALUES (
    'jake',
    'Jake''s Resume',
    'ATS-optimized single-column layout with sans-serif typography, designed for technical roles and engineering positions',
    $$@page {
    size: A4;
    margin: var(--page-margin, 15mm 20mm);
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family, 'Inter', 'Helvetica Neue', Helvetica, Arial, sans-serif);
    color: var(--text-color, #1f2937);
    font-size: var(--font-size, 10pt);
    line-height: var(--line-height, 1.4);
    background: var(--background-color, #ffffff);
}

.resume-page {
    width: 100%;
    max-width: 210mm;
    margin: 0 auto;
    padding: 4mm 8mm;
}

.resume-header {
    margin-bottom: 16px;
    padding-bottom: 0;
}

h1 {
    font-size: var(--heading-font-size, 24pt);
    font-weight: 800;
    color: var(--heading-color, #111827);
    margin-bottom: 2px;
    letter-spacing: -0.5px;
}

.contact-info, .resume-contact {
    font-size: 9pt;
    color: var(--text-color, #4b5563);
    margin-bottom: 0;
    display: inline;
}

.resume-contact a {
    color: var(--primary-color, #1a3a5c);
}

.resume-section {
    margin-bottom: var(--section-spacing, 12px);
}

h2 {
    font-size: var(--section-heading-size, 10pt);
    font-weight: 700;
    color: var(--heading-color, #111827);
    text-transform: uppercase;
    letter-spacing: 1.5px;
    border-bottom: 1px solid var(--primary-color, #1a3a5c);
    padding-bottom: 2px;
    margin-bottom: 6px;
}

h3 {
    font-size: 10pt;
    font-weight: 700;
    color: var(--heading-color, #111827);
    margin-bottom: 2px;
}

p {
    margin-bottom: 4px;
    color: var(--text-color, #1f2937);
}

strong {
    color: var(--heading-color, #111827);
    font-weight: 700;
}

em {
    color: var(--text-color, #1f2937);
    font-style: italic;
}

.resume-item-title {
    font-weight: 700;
    color: var(--heading-color, #111827);
}

.resume-item-date {
    float: right;
    color: var(--text-color, #6b7280);
    font-size: 9pt;
}

.resume-item-subtitle {
    color: var(--text-color, #374151);
    margin-bottom: 2px;
    font-size: 9pt;
}

.clearfix {
    clear: both;
}

ul {
    list-style-type: none;
    margin-left: 0;
    margin-bottom: 4px;
}

li {
    margin-bottom: 2px;
    padding-left: 12px;
    position: relative;
    color: var(--text-color, #1f2937);
}

li::before {
    content: "\2022";
    position: absolute;
    left: 0;
    color: var(--primary-color, #1a3a5c);
    font-weight: bold;
}

.project-block, .resume-project {
    margin-bottom: 8px;
}

a {
    color: var(--primary-color, #1a3a5c);
    text-decoration: none;
}

@media print {
    body { background: var(--background-color, #ffffff); }
    .resume-page { padding: 0; max-width: none; }
}$$,
    true,
    10,
    '[{"name":"--primary-color","type":"color","default":"#1a3a5c","label":"Primary Color","group":"Colors"},{"name":"--text-color","type":"color","default":"#1f2937","label":"Text Color","group":"Colors"},{"name":"--heading-color","type":"color","default":"#111827","label":"Heading Color","group":"Colors"},{"name":"--background-color","type":"color","default":"#ffffff","label":"Background","group":"Colors"},{"name":"--font-family","type":"font","default":"''Inter'', ''Helvetica Neue'', Helvetica, Arial, sans-serif","label":"Font Family","group":"Typography"},{"name":"--font-size","type":"size","default":"10pt","label":"Base Font Size","group":"Typography"},{"name":"--heading-font-size","type":"size","default":"24pt","label":"Name/Title Size","group":"Typography"},{"name":"--section-heading-size","type":"size","default":"10pt","label":"Section Heading Size","group":"Typography"},{"name":"--line-height","type":"size","default":"1.4","label":"Line Height","group":"Typography"},{"name":"--page-margin","type":"size","default":"15mm 20mm","label":"Page Margin","group":"Layout"},{"name":"--section-spacing","type":"size","default":"12px","label":"Section Spacing","group":"Layout"}]',
    'single',
    NULL
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO themes (id, name, description, css_content, is_built_in, sort_order, variables_schema, layout, user_id)
VALUES (
    'academic',
    'Academic CV',
    'Scholarly serif layout with centered header and hanging indentation, suitable for academic and research positions',
    $$@page {
    size: A4;
    margin: var(--page-margin, 22mm 25mm);
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family, 'EB Garamond', Georgia, 'Times New Roman', Times, serif);
    color: var(--text-color, #2d2d2d);
    font-size: var(--font-size, 11.5pt);
    line-height: var(--line-height, 1.5);
    background: var(--background-color, #ffffff);
}

.resume-page {
    width: 100%;
    max-width: 210mm;
    margin: 0 auto;
    padding: 5mm 10mm;
}

.resume-header {
    margin-bottom: 24px;
    padding-bottom: 0;
    text-align: center;
}

h1 {
    font-size: var(--heading-font-size, 20pt);
    font-weight: 700;
    color: var(--heading-color, #1a1a1a);
    margin-bottom: 2px;
    text-align: center;
}

.contact-info, .resume-contact {
    font-size: 10pt;
    color: var(--text-color, #4a4a4a);
    margin-bottom: 2px;
    text-align: center;
}

.resume-contact a {
    color: var(--primary-color, #2d2d2d);
    text-decoration: underline;
}

.resume-section {
    margin-bottom: var(--section-spacing, 16px);
}

h2 {
    font-size: var(--section-heading-size, 11pt);
    font-weight: 700;
    color: var(--heading-color, #1a1a1a);
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 1px solid #1a1a1a;
    padding-bottom: 3px;
    margin-bottom: 8px;
}

h3 {
    font-size: 11pt;
    font-weight: 700;
    color: var(--heading-color, #1a1a1a);
    margin-bottom: 3px;
}

p {
    margin-bottom: 6px;
    color: var(--text-color, #2d2d2d);
}

strong {
    color: var(--heading-color, #1a1a1a);
    font-weight: 700;
}

em {
    color: var(--text-color, #2d2d2d);
    font-style: italic;
}

.resume-item-title {
    font-weight: 700;
    color: var(--heading-color, #1a1a1a);
}

.resume-item-date {
    float: right;
    color: var(--text-color, #555555);
    font-size: 10pt;
}

.resume-item-subtitle {
    color: var(--text-color, #4a4a4a);
    margin-bottom: 3px;
    font-style: italic;
}

.clearfix {
    clear: both;
}

ul {
    list-style-type: none;
    margin-left: 1.5em;
    margin-bottom: 6px;
    text-indent: -1.5em;
    padding-left: 1.5em;
}

li {
    margin-bottom: 3px;
    padding-left: 0;
    position: relative;
    color: var(--text-color, #2d2d2d);
}

li::before {
    content: "";
    position: static;
}

.project-block, .resume-project {
    margin-bottom: 10px;
}

a {
    color: var(--primary-color, #2d2d2d);
    text-decoration: underline;
}

@media print {
    body { background: var(--background-color, #ffffff); }
    .resume-page { padding: 0; max-width: none; }
}$$,
    true,
    11,
    '[{"name":"--primary-color","type":"color","default":"#2d2d2d","label":"Primary Color","group":"Colors"},{"name":"--text-color","type":"color","default":"#2d2d2d","label":"Text Color","group":"Colors"},{"name":"--heading-color","type":"color","default":"#1a1a1a","label":"Heading Color","group":"Colors"},{"name":"--background-color","type":"color","default":"#ffffff","label":"Background","group":"Colors"},{"name":"--font-family","type":"font","default":"''EB Garamond'', Georgia, ''Times New Roman'', Times, serif","label":"Font Family","group":"Typography"},{"name":"--font-size","type":"size","default":"11.5pt","label":"Base Font Size","group":"Typography"},{"name":"--heading-font-size","type":"size","default":"20pt","label":"Name/Title Size","group":"Typography"},{"name":"--section-heading-size","type":"size","default":"11pt","label":"Section Heading Size","group":"Typography"},{"name":"--line-height","type":"size","default":"1.5","label":"Line Height","group":"Typography"},{"name":"--page-margin","type":"size","default":"22mm 25mm","label":"Page Margin","group":"Layout"},{"name":"--section-spacing","type":"size","default":"16px","label":"Section Spacing","group":"Layout"}]',
    'single',
    NULL
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO themes (id, name, description, css_content, is_built_in, sort_order, variables_schema, layout, user_id)
VALUES (
    'swiss',
    'Swiss',
    'Swiss minimalist design with strong grid, generous whitespace, and vermillion accent',
    $$@page {
    size: A4;
    margin: var(--page-margin, 25mm 28mm);
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family, 'Helvetica Neue', Helvetica, Arial, sans-serif);
    color: var(--text-color, #1a1a1a);
    font-size: var(--font-size, 10pt);
    line-height: var(--line-height, 1.6);
    background: var(--background-color, #ffffff);
}

.resume-page {
    width: 100%;
    max-width: 210mm;
    margin: 0 auto;
    padding: 8mm 12mm;
}

.resume-header {
    margin-bottom: 28px;
    padding-bottom: 16px;
    border-bottom: 1px solid #d1d5db;
}

h1 {
    font-size: var(--heading-font-size, 19pt);
    font-weight: 600;
    color: var(--heading-color, #0d0d0d);
    margin-bottom: 6px;
    text-transform: uppercase;
    letter-spacing: 4px;
    text-align: center;
}

.contact-info, .resume-contact {
    font-size: 8.5pt;
    color: var(--text-color, #4b5563);
    margin-bottom: 2px;
    text-align: center;
    text-transform: uppercase;
    letter-spacing: 1.5px;
}

.resume-contact a {
    color: var(--primary-color, #c8102e);
}

.resume-section {
    margin-bottom: var(--section-spacing, 20px);
}

h2 {
    font-size: var(--section-heading-size, 9pt);
    font-weight: 600;
    color: var(--heading-color, #0d0d0d);
    text-transform: uppercase;
    letter-spacing: 3px;
    border-bottom: 2px solid var(--primary-color, #c8102e);
    padding-bottom: 4px;
    margin-bottom: 10px;
}

h3 {
    font-size: 10pt;
    font-weight: 600;
    color: var(--heading-color, #0d0d0d);
    margin-bottom: 3px;
}

p {
    margin-bottom: 6px;
    color: var(--text-color, #1a1a1a);
}

strong {
    color: var(--heading-color, #0d0d0d);
    font-weight: 600;
}

em {
    color: var(--text-color, #1a1a1a);
    font-style: italic;
}

.resume-item-title {
    font-weight: 600;
    color: var(--heading-color, #0d0d0d);
}

.resume-item-date {
    float: right;
    color: var(--text-color, #6b7280);
    font-size: 9pt;
}

.resume-item-subtitle {
    color: var(--text-color, #4b5563);
    margin-bottom: 3px;
}

.clearfix {
    clear: both;
}

ul {
    list-style-type: none;
    margin-left: 0;
    margin-bottom: 6px;
}

li {
    margin-bottom: 2px;
    padding-left: 16px;
    position: relative;
    color: var(--text-color, #1a1a1a);
}

li::before {
    content: "\2013";
    position: absolute;
    left: 0;
    color: var(--primary-color, #c8102e);
}

.project-block, .resume-project {
    margin-bottom: 12px;
}

a {
    color: var(--primary-color, #c8102e);
    text-decoration: none;
}

@media print {
    body { background: var(--background-color, #ffffff); }
    .resume-page { padding: 0; max-width: none; }
}$$,
    true,
    12,
    '[{"name":"--primary-color","type":"color","default":"#c8102e","label":"Primary Color","group":"Colors"},{"name":"--text-color","type":"color","default":"#1a1a1a","label":"Text Color","group":"Colors"},{"name":"--heading-color","type":"color","default":"#0d0d0d","label":"Heading Color","group":"Colors"},{"name":"--background-color","type":"color","default":"#ffffff","label":"Background","group":"Colors"},{"name":"--font-family","type":"font","default":"''Helvetica Neue'', Helvetica, Arial, sans-serif","label":"Font Family","group":"Typography"},{"name":"--font-size","type":"size","default":"10pt","label":"Base Font Size","group":"Typography"},{"name":"--heading-font-size","type":"size","default":"19pt","label":"Name/Title Size","group":"Typography"},{"name":"--section-heading-size","type":"size","default":"9pt","label":"Section Heading Size","group":"Typography"},{"name":"--line-height","type":"size","default":"1.6","label":"Line Height","group":"Typography"},{"name":"--page-margin","type":"size","default":"25mm 28mm","label":"Page Margin","group":"Layout"},{"name":"--section-spacing","type":"size","default":"20px","label":"Section Spacing","group":"Layout"}]',
    'single',
    NULL
)
ON CONFLICT (id) DO NOTHING;

INSERT INTO themes (id, name, description, css_content, is_built_in, sort_order, variables_schema, layout, user_id)
VALUES (
    'harvard',
    'Harvard',
    'Classic MBA-style layout with Times New Roman, centered uppercase name, thick underline section headings, and justified body text',
    $$@page {
    size: A4;
    margin: var(--page-margin, 20mm 22mm);
}

* {
    box-sizing: border-box;
    margin: 0;
    padding: 0;
}

body {
    font-family: var(--font-family, 'Times New Roman', Times, serif);
    color: var(--text-color, #000000);
    font-size: var(--font-size, 11pt);
    line-height: var(--line-height, 1.4);
    background: var(--background-color, #ffffff);
}

.resume-page {
    width: 100%;
    max-width: 210mm;
    margin: 0 auto;
    padding: 4mm 8mm;
}

.resume-header {
    margin-bottom: 20px;
    padding-bottom: 10px;
    border-bottom: 1px solid #000000;
    text-align: center;
}

h1 {
    font-size: var(--heading-font-size, 18pt);
    font-weight: 700;
    color: var(--heading-color, #000000);
    margin-bottom: 3px;
    text-transform: uppercase;
    letter-spacing: 1px;
    text-align: center;
}

.contact-info, .resume-contact {
    font-size: 10pt;
    color: var(--text-color, #000000);
    margin-bottom: 2px;
    text-align: center;
}

.resume-contact a {
    color: var(--primary-color, #000000);
    text-decoration: none;
}

.resume-section {
    margin-bottom: var(--section-spacing, 14px);
}

h2 {
    font-size: var(--section-heading-size, 11pt);
    font-weight: 700;
    color: var(--heading-color, #000000);
    text-transform: uppercase;
    letter-spacing: 1px;
    border-bottom: 2px solid #000000;
    padding-bottom: 1px;
    margin-bottom: 6px;
}

h3 {
    font-size: 11pt;
    font-weight: 700;
    color: var(--heading-color, #000000);
    margin-bottom: 3px;
}

p {
    margin-bottom: 5px;
    color: var(--text-color, #000000);
    text-align: justify;
}

strong {
    color: var(--heading-color, #000000);
    font-weight: 700;
}

em {
    color: var(--text-color, #000000);
    font-style: italic;
}

.resume-item-title {
    font-weight: 700;
    color: var(--heading-color, #000000);
}

.resume-item-date {
    float: right;
    color: var(--text-color, #000000);
    font-size: 10pt;
}

.resume-item-subtitle {
    color: var(--text-color, #000000);
    margin-bottom: 3px;
    font-style: italic;
}

.clearfix {
    clear: both;
}

ul {
    list-style-type: none;
    margin-left: 0;
    margin-bottom: 5px;
}

li {
    margin-bottom: 2px;
    padding-left: 18px;
    position: relative;
    color: var(--text-color, #000000);
    text-indent: 0;
}

li::before {
    content: "\2022";
    position: absolute;
    left: 4px;
    color: #000000;
    font-weight: bold;
}

.project-block, .resume-project {
    margin-bottom: 8px;
}

a {
    color: var(--primary-color, #000000);
    text-decoration: underline;
}

@media print {
    body { background: var(--background-color, #ffffff); }
    .resume-page { padding: 0; max-width: none; }
}$$,
    true,
    13,
    '[{"name":"--primary-color","type":"color","default":"#000000","label":"Primary Color","group":"Colors"},{"name":"--text-color","type":"color","default":"#000000","label":"Text Color","group":"Colors"},{"name":"--heading-color","type":"color","default":"#000000","label":"Heading Color","group":"Colors"},{"name":"--background-color","type":"color","default":"#ffffff","label":"Background","group":"Colors"},{"name":"--font-family","type":"font","default":"''Times New Roman'', Times, serif","label":"Font Family","group":"Typography"},{"name":"--font-size","type":"size","default":"11pt","label":"Base Font Size","group":"Typography"},{"name":"--heading-font-size","type":"size","default":"18pt","label":"Name/Title Size","group":"Typography"},{"name":"--section-heading-size","type":"size","default":"11pt","label":"Section Heading Size","group":"Typography"},{"name":"--line-height","type":"size","default":"1.4","label":"Line Height","group":"Typography"},{"name":"--page-margin","type":"size","default":"20mm 22mm","label":"Page Margin","group":"Layout"},{"name":"--section-spacing","type":"size","default":"14px","label":"Section Spacing","group":"Layout"}]',
    'single',
    NULL
)
ON CONFLICT (id) DO NOTHING;
