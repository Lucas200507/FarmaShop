// ...existing code...
import FarmaInputField from '@/components/FarmaInputField';
import FarmaButton from '@/components/FarmaButtonBlue';
import FarmaButtonGreen from '@/components/FarmaButtonGreen';
import FarmaHeader from '@/components/FarmaHeader';
import React, { useState } from 'react';
import { View, Text, StyleSheet, Platform, StatusBar as RNStatusBar } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useRouter } from "expo-router";
import FarmaApprovedSheet from '@/components/FarmaApprovedSheet';

export default function LoginScreen() {
    const insets = useSafeAreaInsets();
    const topInset = insets.top || (Platform.OS === 'android' ? (RNStatusBar.currentHeight ?? 0) : 0);

    const [usuario, setUsuario] = useState("");
    const [senha, setSenha] = useState("");
    const [showErrorSheet, setshowErrorSheet] = useState(false);

    const router = useRouter();
    let timeoutId: ReturnType<typeof setTimeout> | null = null;

    const handleSubmit = async () => {
      let resultado = await logar();
      if (!resultado.sucesso) {
        setshowErrorSheet(true);

        if (timeoutId) clearTimeout(timeoutId);

        timeoutId = setTimeout(() => {
            setshowErrorSheet(false);
            timeoutId = null;
        }, 3000);
      } else {
        router.navigate("/Home/HomeScreen");
      }
    }
    
async function logar() {
  const dados = {
    email: usuario,
    senha: senha
  };

  try {
    const resposta = await fetch("http://localhost:8080/api/auth/login", {
      method: "POST",
      headers: { "Content-Type": "application/json" },
      body: JSON.stringify(dados)
    });

    // Se o status NÃO for 2xx, já lança erro
    if (!resposta.ok) {
      const erro = await resposta.json();
      console.log("Erro:", erro);
      return { sucesso: false, mensagem: erro.message || "Falha no login" };
    }

    // Se der sucesso
    const resultado = await resposta.json();
    console.log("Sucesso:", resultado);

    return { sucesso: true, dados: resultado };

  } catch (e) {
    console.log("Erro inesperado:", e);
    return { sucesso: false, mensagem: "Erro de conexão com o servidor" };
  }
}



    return (
        <View style={[styles.container, { paddingTop: topInset }]}>
            <FarmaHeader title="Login" />

            <View style={styles.content}>
                <FarmaInputField 
                    label='Usuário' 
                    required= {true}
                    value={usuario}
                    onChangeText={setUsuario}
                />

                <FarmaInputField 
                    label='Senha' 
                    required= {true}
                    value={senha}
                    onChangeText={setSenha}
                />

                <FarmaButton title="Entrar" onPress={handleSubmit} />

                
            </View>
            {showErrorSheet && (
                <FarmaApprovedSheet title="Usuário ou Senha inválidos! Digite novamente." />
            )}
        </View>
  );
}

const styles = StyleSheet.create({
  container: { 
    flex: 1, 
    backgroundColor: '#000038', 
    alignItems: 'center'
  },
  header: {
    width: '100%',
    backgroundColor: '#fff',
    paddingHorizontal: 20,
    paddingTop: 20,
    paddingBottom: 60,
    marginBottom: 40,
    // bordas inferiores arredondadas
    borderBottomLeftRadius: 60,
    borderBottomRightRadius: 60,
    alignItems: 'center',
    justifyContent: 'center',
    elevation: 3,
    shadowColor: '#000',
    shadowOffset: { width: 0, height: 1 },
    shadowOpacity: 0.08,
    shadowRadius: 6,
  },
 logo: {
  width: 96,
  height: 96,
  position: 'absolute',
  top: 95, // ajusta este valor conforme a altura do seu header
  alignSelf: 'center',
  backgroundColor: '#000038',
  borderRadius: 48,
  padding: 10,
  zIndex: 10, // garante que fique por cima
},
  title: { 
    fontSize: 24, 
    fontWeight: '500', 
    color: '#000038' },
  content: { 
    flex: 1,
    alignItems: 'center',
    gap: 20,
    padding: 20, 
    marginTop: 8 
  },
  text: {
    color: '#fff',
    fontWeight: '400',
    fontSize: 30,
  }, 
  link: {
    color: '#6AD7BF',
    fontWeight: '400',
    fontSize: 25,
  }
});
