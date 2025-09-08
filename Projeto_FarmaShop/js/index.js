function Modalesqueceu_senha(){
    $.ajax({
        method: "GET",
        url: "../Pages/Modais/esqueceuSenha.html",
        success: function(data){
            $("#conteudo_modal_login").html(data);
            let modal_login = new bootstrap.Modal(document.getElementById("modal_login"));
            modal_login.toggle();                
        }
    })
}

function verificarEmail(email){

}

function validar_email(){
    const email = $("email_esqSenha");
    $("form_esqSenha").validate({
        rules: {
            email: {
                required: true,
                email: true,
                // Verificar se já tem o email cadastrado
                remote: {
                    url: "index.js/verificarEmail" + email // Verificar no bando
                }
            }
        },
        messages: {
            email: {
                required: "O email precisa ser preenchido!",
                email: "Digite um email válido",
                
            }
        }
    })
}

function enviar_email_esqSenha(){
    let conteudo_modal = document.getElementById("modal_login_conteudo");
    
}