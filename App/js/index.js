function Modalesqueceu_senha(){
    $.ajax({
        method: "GET",
        url: "../Pages/Modais/esqueceuSenha.html",
        success: function(data){
            $("#modal_login").html(data);
            let modal_login = new bootstrap.Modal(document.getElementById("modal_login"));
            modal_login.toggle();                
        }
    });
}

function verificarEmail(email){

}

function validar_email(email){    
    $("#form_esqSenha").validate({
        rules: {
            email: {
                required: true,
                email: true,
                // Verificar se já tem o email cadastrado
                remote: {
                    url: "../Controller/UsuarioController.java",
                    type: "get",
                    data:{
                        email: function(){
                            return $("#email_verificador").val();
                        }
                    }
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

function enviar_email_esqSenha(email){
    validar_email(email);
    let conteudo_modal = document.getElementById("modal_esqueceu_senha");
    
}

function  definir_iconPerson(letra, situacao){
    if (situacao  == 'logado'){
       $.ajax({
        url: "icones.json",
        method: "GET",
        dataType: "json",
        success: function(resultado){
            let padding = resultado[letra] || "0.25em 0.6em";
            $("#icon_deslogado").hide(); // some
                $("#icon_logado")
                    .show()
                    .css("padding", padding) // aplica o padding
                    .html(letra);       

        }
       });
    } else {
        $("#icon_logado").hide();
        $("#icon_deslogado").show();
    }
}