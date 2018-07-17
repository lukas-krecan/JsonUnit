<html>
<head>
    <meta http-equiv = "content-type" content = "text/html; charset=UTF-8">
    <script type='text/javascript' src="https://cdn.jsdelivr.net/npm/jsondiffpatch/dist/jsondiffpatch.umd.min.js" charset="utf-8"></script>
</head>
<style type="text/css">
    body {
        font-family: Georgia, serif;
        min-width: 600px;
    }

    h1 {
        margin: 4px;
    }

    header > * {
        display: inline-block;
    }

    #description {
        margin-left: 10px;
        font-size: x-large;
    }

    #external-link {
        font-size: smaller;
        vertical-align: top;
        margin-top: 10px;
    }

    h2 {
        display: inline-block;
        margin: 5px 0;
    }


    a#fork_me {
        position: absolute;
        top: 0;
        right: 0;
    }

    .json-input > div {
        float: left;
        width: 50%;
    }

    .json-input > div
    {
        text-align: center;
    }

    .CodeMirror
    {
        text-align: initial;
        border: 1px solid #ccc;
    }

    .json-input > div > textarea {
        width: 95%;
        height: 200px;
    }

    .prettyfy {
        font-weight: bold;
        font-size: smaller;
        margin-left: 5px;
    }

    .buttons {
        width: 100%;
        text-align:center;
        height: 1px;
    }

    .buttons > div
    {
        margin: 0 auto;
    }

    .json-error {
        background: #ffdfdf;
        -webkit-transition: all 1s;
        transition: all 1s;
    }

    .error-message {
        font-weight: bold;
        color: red;
        font-size: smaller;
        min-height: 20px;
        display: block;
    }

    .header-options {
        font-weight: normal;
        margin-left: 30px;
        display: inline-block;
    }

    #delta-panel-visual {
        width: 100%;
        overflow: auto;
    }

    #visualdiff {
        margin-top: 4px;
    }

    #json-delta {
        font-family: 'Bitstream Vera Sans Mono', 'DejaVu Sans Mono', Monaco, Courier, monospace;
        font-size: 12px;
        margin: 0;
        padding: 0;
        width: 100%;
        height: 200px;
    }

    #delta-panel-json > p {
        margin: 4px;
    }

    footer {
        font-size: small;
        text-align: center;
        margin: 40px;
    }

    .credits {
        font-size: smaller;
    }

    .credits a {
        text-decoration: none;
        color: black;
    }

    .credits a:hover {
        text-decoration: underline;
    }

    .results {
        margin-top: 20px;
    }

    .results > div {
        vertical-align: top;
        display: inline-block;
    }

    .jsondiffpatch-delta {
        font-family: 'Bitstream Vera Sans Mono', 'DejaVu Sans Mono', Monaco, Courier, monospace;
        font-size: 12px;
        margin: 0;
        padding: 0 0 0 12px;
        display: inline-block;
    }
    .jsondiffpatch-delta pre {
        font-family: 'Bitstream Vera Sans Mono', 'DejaVu Sans Mono', Monaco, Courier, monospace;
        font-size: 12px;
        margin: 0;
        padding: 0;
        display: inline-block;
    }
    ul.jsondiffpatch-delta {
        list-style-type: none;
        padding: 0 0 0 20px;
        margin: 0;
    }
    .jsondiffpatch-delta ul {
        list-style-type: none;
        padding: 0 0 0 20px;
        margin: 0;
    }
    .jsondiffpatch-added .jsondiffpatch-property-name,
    .jsondiffpatch-added .jsondiffpatch-value pre,
    .jsondiffpatch-modified .jsondiffpatch-right-value pre,
    .jsondiffpatch-textdiff-added {
        background: #bbffbb;
    }
    .jsondiffpatch-deleted .jsondiffpatch-property-name,
    .jsondiffpatch-deleted pre,
    .jsondiffpatch-modified .jsondiffpatch-left-value pre,
    .jsondiffpatch-textdiff-deleted {
        background: #ffbbbb;
        text-decoration: line-through;
    }
    .jsondiffpatch-unchanged,
    .jsondiffpatch-movedestination {
        color: gray;
    }
    .jsondiffpatch-unchanged,
    .jsondiffpatch-movedestination > .jsondiffpatch-value {
        transition: all 0.5s;
        -webkit-transition: all 0.5s;
        overflow-y: hidden;
    }
    .jsondiffpatch-unchanged-showing .jsondiffpatch-unchanged,
    .jsondiffpatch-unchanged-showing .jsondiffpatch-movedestination > .jsondiffpatch-value {
        max-height: 100px;
    }
    .jsondiffpatch-unchanged-hidden .jsondiffpatch-unchanged,
    .jsondiffpatch-unchanged-hidden .jsondiffpatch-movedestination > .jsondiffpatch-value {
        max-height: 0;
    }
    .jsondiffpatch-unchanged-hiding .jsondiffpatch-movedestination > .jsondiffpatch-value,
    .jsondiffpatch-unchanged-hidden .jsondiffpatch-movedestination > .jsondiffpatch-value {
        display: block;
    }
    .jsondiffpatch-unchanged-visible .jsondiffpatch-unchanged,
    .jsondiffpatch-unchanged-visible .jsondiffpatch-movedestination > .jsondiffpatch-value {
        max-height: 100px;
    }
    .jsondiffpatch-unchanged-hiding .jsondiffpatch-unchanged,
    .jsondiffpatch-unchanged-hiding .jsondiffpatch-movedestination > .jsondiffpatch-value {
        max-height: 0;
    }
    .jsondiffpatch-unchanged-showing .jsondiffpatch-arrow,
    .jsondiffpatch-unchanged-hiding .jsondiffpatch-arrow {
        display: none;
    }
    .jsondiffpatch-value {
        display: inline-block;
    }
    .jsondiffpatch-property-name {
        display: inline-block;
        padding-right: 5px;
        vertical-align: top;
    }
    .jsondiffpatch-property-name:after {
        content: ': ';
    }
    .jsondiffpatch-child-node-type-array > .jsondiffpatch-property-name:after {
        content: ': [';
    }
    .jsondiffpatch-child-node-type-array:after {
        content: '],';
    }
    div.jsondiffpatch-child-node-type-array:before {
        content: '[';
    }
    div.jsondiffpatch-child-node-type-array:after {
        content: ']';
    }
    .jsondiffpatch-child-node-type-object > .jsondiffpatch-property-name:after {
        content: ': {';
    }
    .jsondiffpatch-child-node-type-object:after {
        content: '},';
    }
    div.jsondiffpatch-child-node-type-object:before {
        content: '{';
    }
    div.jsondiffpatch-child-node-type-object:after {
        content: '}';
    }
    .jsondiffpatch-value pre:after {
        content: ',';
    }
    li:last-child > .jsondiffpatch-value pre:after,
    .jsondiffpatch-modified > .jsondiffpatch-left-value pre:after {
        content: '';
    }
    .jsondiffpatch-modified .jsondiffpatch-value {
        display: inline-block;
    }
    .jsondiffpatch-modified .jsondiffpatch-right-value {
        margin-left: 5px;
    }
    .jsondiffpatch-moved .jsondiffpatch-value {
        display: none;
    }
    .jsondiffpatch-moved .jsondiffpatch-moved-destination {
        display: inline-block;
        background: #ffffbb;
        color: #888;
    }
    .jsondiffpatch-moved .jsondiffpatch-moved-destination:before {
        content: ' => ';
    }
    ul.jsondiffpatch-textdiff {
        padding: 0;
    }
    .jsondiffpatch-textdiff-location {
        color: #bbb;
        display: inline-block;
        min-width: 60px;
    }
    .jsondiffpatch-textdiff-line {
        display: inline-block;
    }
    .jsondiffpatch-textdiff-line-number:after {
        content: ',';
    }
    .jsondiffpatch-error {
        background: red;
        color: white;
        font-weight: bold;
    }
</style>
<body>
<div id="visual"></div>
<script>
    var left = ${data.actual};
    var right = ${data.expected};
    var delta = ${data.patch};

    document.getElementById('visual').innerHTML = jsondiffpatch.formatters.html.format(delta, left);
</script>
</body>
</html>