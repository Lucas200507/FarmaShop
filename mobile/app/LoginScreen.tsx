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

    const handleSubmit = () => {
        setshowErrorSheet(true);

        if (timeoutId) clearTimeout(timeoutId);

        timeoutId = setTimeout(() => {
            setshowErrorSheet(false);
            timeoutId = null;
        }, 3000);
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
