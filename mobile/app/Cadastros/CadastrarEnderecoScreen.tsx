import FarmaInputField from '@/components/FarmaInputField';
import FarmaButton from '@/components/FarmaButtonBlue';
import FarmaHeader from '@/components/FarmaHeader';
import React, { useState } from 'react';
import { View, Text, StyleSheet, Platform, StatusBar as RNStatusBar } from 'react-native';
import { useSafeAreaInsets } from 'react-native-safe-area-context';
import { useRouter } from "expo-router";
import FarmaApprovedSheet from '@/components/FarmaApprovedSheet';

export default function CadastrarEnderecoScreen() {
    const insets = useSafeAreaInsets();
        const topInset = insets.top || (Platform.OS === 'android' ? (RNStatusBar.currentHeight ?? 0) : 0);
    
        const [cep, setCep] = useState("");
        const [estado, setEstado] = useState("");
        const [cidade, setCidade] = useState("");
        const [rua, setRua] = useState("");
        const [bairro, setBairro] = useState("");
        const [complemento, setComplemento] = useState("");
        const [showErrorSheet, setshowErrorSheet] = useState(false);
        const [errorMessage, setErrorMessage] = useState("");

        const router = useRouter();
        let timeoutId: ReturnType<typeof setTimeout> | null = null;

        const timerError =  (errorMensage: string) => {
            setErrorMessage(errorMensage);
            setshowErrorSheet(true);
            
            if (timeoutId) clearTimeout(timeoutId);
    
            timeoutId = setTimeout(() => {
                setshowErrorSheet(false);
                timeoutId = null;
            }, 3000);
        }

        const handleSubmit = () => {
            if (cep === "" || estado === "" || cidade === "" || rua === ""
                || bairro === "" || complemento === ""
            ) {
                timerError("Existem campos em branco.");
                return;
            }
            else if (cep.length !== 14) {
                timerError("CNPJ inválido. Digite um CNPJ que realmente exista.");
                return;
            }
            router.navigate("/Home/HomeScreen");
        }
        
        return (
            <View style={[styles.container, { paddingTop: topInset }]}>
                <FarmaHeader title="Cadastro" />
            
                <View style={styles.content}>
                    <Text style={styles.text}> Dados da Empresa </Text>

                    <FarmaInputField 
                        label='CEP' 
                        required= {true}
                        value={cep}
                        onChangeText={setCep}
                    />
    
                    <FarmaInputField 
                        label='Estado' 
                        required= {true}
                        value={estado}
                        onChangeText={setEstado}
                    />

                    <FarmaInputField 
                        label='Cidade' 
                        required= {true}
                        value={cidade}
                        onChangeText={setCidade}
                    />

                    <FarmaInputField 
                        label='Rua' 
                        required= {true}
                        value={rua}
                        onChangeText={setRua}
                    />

                    <FarmaInputField 
                        label='Bairro' 
                        required= {true}
                        value={bairro}
                        onChangeText={setBairro}
                    />

                    <FarmaInputField 
                        label='Complemento' 
                        required= {false}
                        value={complemento}
                        onChangeText={setComplemento}
                    />
                    <FarmaButton title="Entrar" onPress={handleSubmit} />
    
                    
                </View>
                {showErrorSheet && (
                    <FarmaApprovedSheet title={errorMessage} />
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
});
