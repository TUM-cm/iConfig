package de.tum.in.cm.android.eddystonemanager.backend;

import android.util.Base64;
import android.util.Log;

import java.io.IOException;
import java.io.InputStream;
import java.security.KeyManagementException;
import java.security.KeyStore;
import java.security.KeyStoreException;
import java.security.NoSuchAlgorithmException;
import java.security.cert.Certificate;
import java.security.cert.CertificateException;
import java.security.cert.CertificateFactory;
import java.util.List;
import java.util.concurrent.TimeUnit;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManager;
import javax.net.ssl.TrustManagerFactory;
import javax.net.ssl.X509TrustManager;

import de.tum.in.cm.android.eddystonemanager.gui.MainActivity;
import de.tum.in.cm.android.eddystonemanager.utils.general.FileUtils;
import okhttp3.Interceptor;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import retrofit2.Converter;
import retrofit2.Retrofit;

public class ServiceGenerator {

  public static final String TAG = ServiceGenerator.class.getSimpleName();
  private static final OkHttpClient.Builder httpClient = new OkHttpClient.Builder();

  public static Retrofit.Builder createBuilder(String baseUrl,
                                               List<Converter.Factory> converters) {
    Retrofit.Builder builder = new Retrofit.Builder().baseUrl(baseUrl);
    for (Converter.Factory converter : converters) {
      builder.addConverterFactory(converter);
    }
    return builder;
  }

  public static Retrofit.Builder createBuilder(String baseUrl, Converter.Factory converter) {
    Retrofit.Builder builder = new Retrofit.Builder().baseUrl(baseUrl);
    builder.addConverterFactory(converter);
    return builder;
  }

  public static <S> S createService(Class<S> serviceClass, Retrofit.Builder builder) {
    return createService(serviceClass, builder, null, null);
  }

  public static <S> S createService(Class<S> serviceClass, Retrofit.Builder builder,
                                    String username, String password) {
    return createService(serviceClass, builder, username, password, false);
  }

  public static <S> S createService(Class<S> serviceClass, Retrofit.Builder builder,
                                    String username, String password, boolean ssl) {
    if (username != null && password != null) {
      String credentials = username + ":" + password;
      final String basic = "Basic " + Base64.encodeToString(credentials.getBytes(),
              Base64.NO_WRAP);
      httpClient.addInterceptor(new Interceptor() {
        @Override
        public Response intercept(Interceptor.Chain chain) throws IOException {
          Request original = chain.request();
          Request.Builder requestBuilder = original.newBuilder()
                  .header("Authorization", basic)
                  .method(original.method(), original.body());
          Request request = requestBuilder.build();
          return chain.proceed(request);
        }
      });
    }
    if (ssl) {
      try {
        SSLParameter sslParameter = getSSLConfig();
        httpClient.sslSocketFactory(sslParameter.getSslContext().getSocketFactory(),
                sslParameter.getTrustManager());
      } catch (CertificateException e) {
        Log.d(TAG, "CertificateException", e);
      } catch (IOException e) {
        Log.d(TAG, "IOException", e);
      } catch (KeyStoreException e) {
        Log.d(TAG, "KeyStoreException", e);
      } catch (NoSuchAlgorithmException e) {
        Log.d(TAG, "NoSuchAlgorithmException", e);
      } catch (KeyManagementException e) {
        Log.d(TAG, "KeyManagementException", e);
      }
    }
    httpClient.connectTimeout(2, TimeUnit.MINUTES);
    httpClient.readTimeout(2, TimeUnit.MINUTES);
    OkHttpClient client = httpClient.build();
    Retrofit retrofit = builder.client(client).build();
    return retrofit.create(serviceClass);
  }

  private static SSLParameter getSSLConfig() throws CertificateException, IOException,
          KeyStoreException, NoSuchAlgorithmException, KeyManagementException {
    CertificateFactory cf = CertificateFactory.getInstance("X.509");
    try (InputStream cert = FileUtils.getInputStream(MainActivity.getCertificate())) {
      Certificate ca = cf.generateCertificate(cert);
      // Creating a KeyStore containing our trusted CAs
      String keyStoreType = KeyStore.getDefaultType();
      KeyStore keyStore = KeyStore.getInstance(keyStoreType);
      keyStore.load(null, null);
      keyStore.setCertificateEntry("ca", ca);
      // Creating a TrustManager that trusts the CAs in our KeyStore.
      String tmfAlgorithm = TrustManagerFactory.getDefaultAlgorithm();
      TrustManagerFactory tmf = TrustManagerFactory.getInstance(tmfAlgorithm);
      tmf.init(keyStore);
      // Creating an SSLSocketFactory that uses our TrustManager
      SSLContext sslContext = SSLContext.getInstance("TLS");
      sslContext.init(null, tmf.getTrustManagers(), null);
      TrustManager[] trustManagers = tmf.getTrustManagers();
      X509TrustManager trustManager = (X509TrustManager) trustManagers[0];
      return new SSLParameter(sslContext, trustManager);
    }
  }

  private static class SSLParameter {

    private final SSLContext sslContext;
    private final X509TrustManager trustManager;

    public SSLParameter(SSLContext sslContext, X509TrustManager trustManager) {
      this.sslContext = sslContext;
      this.trustManager = trustManager;
    }

    public SSLContext getSslContext() {
      return this.sslContext;
    }

    public X509TrustManager getTrustManager() {
      return this.trustManager;
    }

  }

}
