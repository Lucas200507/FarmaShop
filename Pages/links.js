// Comunicação entre páginas

// CONEXÃO COM BOOSTRAP CSS
const boostrapCss = document.createElement('link');
boostrapCss.crossOrigin = 'anonymous';
boostrapCss.href = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/css/bootstrap.min.css";
boostrapCss.integrity = "sha384-sRIl4kxILFvY47J16cr9ZwB07vP4J8+LH7qKQnuqkuIAvNWLzeN8tE5YBujZqJLB";
boostrapCss.rel = "stylesheet";
document.head.appendChild(boostrapCss);
// CONEXÃO COM INDEX CSS
const IndexCss = document.createElement('link');
IndexCss.rel = "stylesheet";
IndexCss.href = "../css/index.css";
document.head.appendChild(IndexCss);
// CONEXÃO COM BOOTSTRAP JS
const boostrapJS = document.createElement('script');
boostrapJS.src = "https://cdn.jsdelivr.net/npm/bootstrap@5.3.8/dist/js/bootstrap.bundle.min.js";
boostrapJS.integrity = "sha384-FKyoEForCGlyvwx9Hj09JcYn3nv7wiPVlz7YYwJrWVcXK/BmnVDxM+D2scQbITxI";
boostrapJS.crossOrigin = "anonymous";
document.body.appendChild(boostrapJS);
// CONEXÃO COM INDEX JS
const IndexJS = document.createElement('script');
IndexJS.src = "../js/index.js";
document.head.appendChild(IndexJS);
// CONEXÃO COM IONICONS
const ionicons = document.createElement('script');
ionicons.type = "module";
ionicons.src = "https://unpkg.com/ionicons@7.1.0/dist/ionicons/ionicons.esm.js";
document.body.appendChild(ionicons);
// CONEXÃO COM IONICONS SEM MODULE
const ioniconsNoModule = document.createElement('script');
ioniconsNoModule.noModule = true;
ioniconsNoModule.src = "https://unpkg.com/ionicons@7.1.0/dist/ionicons/ionicons.js";
document.body.appendChild(ioniconsNoModule);
// CONEXÃO COM JQUERY
const jquery = document.createElement('script');
jquery.src = "https://ajax.googleapis.com/ajax/libs/jquery/3.7.1/jquery.min.js";
document.head.appendChild(jquery);
// CONEXÃO COM JQUERY VALIDATE
const jqueyValidate = document.createElement('script');
jqueyValidate.src = "https://cdn.jsdelivr.net/npm/jquery-validation@1.19.5/dist/jquery.validate.min.js";

// TEXTO FLUTUANTE AO PASSAR O MOUSE (BOOSTRAP)
document.addEventListener('DOMContentLoaded', function() {
    const tooltipTriggerList = document.querySelectorAll('[data-bs-toggle="tooltip"]');
    [...tooltipTriggerList].map(tooltipTriggerEl => new boostrapCss.Tooltip(tooltipTriggerEl));
});
