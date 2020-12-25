package com.iitp.iitp_demo.api;


import android.util.Log;

import androidx.annotation.NonNull;
import androidx.lifecycle.MutableLiveData;

import com.google.android.gms.tasks.OnCompleteListener;
import com.google.android.gms.tasks.Task;
import com.google.firebase.iid.FirebaseInstanceId;
import com.google.firebase.iid.InstanceIdResult;
import com.iitp.iitp_demo.api.model.delegaterVCVo;
import com.iitp.iitp_demo.api.model.pushFriendVo;
import com.iitp.iitp_demo.api.model.pushRegisterVo;
import com.iitp.iitp_demo.api.model.pushResponseVo;
import com.iitp.iitp_demo.util.PrintLog;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.concurrent.TimeUnit;

import okhttp3.OkHttpClient;
import okhttp3.logging.HttpLoggingInterceptor;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Response;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;
import retrofit2.converter.scalars.ScalarsConverterFactory;

public class AgentAPI{

    private static AgentAPI _agentApi = null;
    public final AgentAPIInfo agentAPIInfo;
    private final Retrofit retrofit;


    public AgentAPI(){
        OkHttpClient.Builder httpClient = new OkHttpClient.Builder();
        HttpLoggingInterceptor loggingInterceptor = new HttpLoggingInterceptor(new HttpLoggingInterceptor.Logger(){
            @Override
            public void log(@NonNull String message){
                Log.d("Verification", message);
            }
        });

        loggingInterceptor.setLevel(HttpLoggingInterceptor.Level.BODY);

        httpClient = new OkHttpClient.Builder();
        httpClient.interceptors().add(loggingInterceptor);

        httpClient.readTimeout(30, TimeUnit.SECONDS);
        httpClient.writeTimeout(15, TimeUnit.SECONDS);
        retrofit = new Retrofit.Builder()
                .addConverterFactory(ScalarsConverterFactory.create())
                .addConverterFactory(GsonConverterFactory.create())
                .baseUrl(AgentAPIInfo.BaseUrl)
                .client(httpClient.build())
                .build();
        agentAPIInfo = retrofit.create(AgentAPIInfo.class);
    }

    public static AgentAPI getInstance(){

        if(_agentApi == null){
            _agentApi = new AgentAPI();
        }
        return _agentApi;
    }

    /**
     * FCM 토큰 등록
     *
     * @param result reponse
     */
    public void registerToken(MutableLiveData<pushResponseVo> result, String did){
        FirebaseInstanceId.getInstance().getInstanceId().addOnCompleteListener(new OnCompleteListener<InstanceIdResult>(){
            @Override
            public void onComplete(@NonNull Task<InstanceIdResult> task){
                if(!task.isSuccessful()){
                    PrintLog.e("getInstanceId failed : " + task.getException());
                    return;
                }
                String token = task.getResult().getToken();
                PrintLog.e("token = " + task.getResult().getToken());
                PrintLog.e("tokenid = " + task.getResult().getId());
                sendTokenData(did, token, result);
            }
        });
    }

    /**
     * FCM 토큰등록 api
     *
     * @param context context
     * @param token   fcm token
     * @param result  response
     */
    private void sendTokenData(String did, String token, MutableLiveData<pushResponseVo> result){
        pushRegisterVo body = new pushRegisterVo(did, token);
        Call<pushResponseVo> rtn = agentAPIInfo.registerToken(body);
        rtn.enqueue(new Callback<pushResponseVo>(){
            @Override
            public void onResponse(@NotNull Call<pushResponseVo> call, @NotNull Response<pushResponseVo> response){
                if(response.code() == 200){
                    if(result != null){
                        result.setValue(response.body());
                    }
                }else{
                    if(response.errorBody() != null){
                        String errorMessage;
                        try{
                            errorMessage = response.errorBody().string();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<pushResponseVo> call, @NotNull Throwable t){
                if(result != null){
                    result.setValue(null);
                }
                PrintLog.crash("onFailure" + t.getMessage());
            }
        });
    }

    /**
     * 친구목록 가져오기
     *
     * @param did
     * @param result
     */
    public void getFriendsList(String did, MutableLiveData<pushResponseVo> result){
        pushRegisterVo body = new pushRegisterVo(did, null);
        Call<pushResponseVo> rtn = agentAPIInfo.requestFriendsList(body);
        rtn.enqueue(new Callback<pushResponseVo>(){
            @Override
            public void onResponse(@NotNull Call<pushResponseVo> call, @NotNull Response<pushResponseVo> response){
                if(response.code() == 200){
                    if(result != null){
                        result.setValue(response.body());
                    }
                }else{
                    if(response.errorBody() != null){
                        String errorMessage;
                        try{
                            errorMessage = response.errorBody().string();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<pushResponseVo> call, @NotNull Throwable t){
                if(result != null){
                    result.setValue(null);
                }
                PrintLog.crash("onFailure" + t.getMessage());
            }
        });
    }


    public void requestCreateFriends(String did, String name,MutableLiveData<pushResponseVo> result){
        pushFriendVo body = new pushFriendVo(did, name, null);
        Call<pushResponseVo> rtn = agentAPIInfo.requestCreateFriends(body);
        rtn.enqueue(new Callback<pushResponseVo>(){
            @Override
            public void onResponse(@NotNull Call<pushResponseVo> call, @NotNull Response<pushResponseVo> response){
                if(response.code() == 200){
                    if(result != null){
                        result.setValue(response.body());
                    }
                }else{
                    if(response.errorBody() != null){
                        String errorMessage;
                        try{
                            errorMessage = response.errorBody().string();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<pushResponseVo> call, @NotNull Throwable t){
                if(result != null){
                    result.setValue(null);
                }
                PrintLog.crash("onFailure" + t.getMessage());
            }
        });
    }

    public void requestResetFriendsList(MutableLiveData<pushResponseVo> result){
        Call<pushResponseVo> rtn = agentAPIInfo.resetFriends("0000");
        rtn.enqueue(new Callback<pushResponseVo>(){
            @Override
            public void onResponse(@NotNull Call<pushResponseVo> call, @NotNull Response<pushResponseVo> response){
                if(response.code() == 200){
                    if(result != null){
                        result.setValue(response.body());
                    }
                }else{
                    if(response.errorBody() != null){
                        String errorMessage;
                        try{
                            errorMessage = response.errorBody().string();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<pushResponseVo> call, @NotNull Throwable t){
                if(result != null){
                    result.setValue(null);
                }
                PrintLog.crash("onFailure" + t.getMessage());
            }
        });
    }


    /**
     * 친구목록 가져오기
     *
     * @param did
     * @param result
     */
    public void sendcredential(delegaterVCVo body, MutableLiveData<pushResponseVo> result){
        Call<pushResponseVo> rtn = agentAPIInfo.sendCredential(body);
        rtn.enqueue(new Callback<pushResponseVo>(){
            @Override
            public void onResponse(@NotNull Call<pushResponseVo> call, @NotNull Response<pushResponseVo> response){
                if(response.code() == 200){
                    if(result != null){
                        result.setValue(response.body());
                    }
                }else{
                    if(response.errorBody() != null){
                        String errorMessage;
                        try{
                            errorMessage = response.errorBody().string();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<pushResponseVo> call, @NotNull Throwable t){
                if(result != null){
                    result.setValue(null);
                }
                PrintLog.crash("onFailure" + t.getMessage());
            }
        });
    }

    public void requestGetVC(String token, MutableLiveData<pushResponseVo> result){
        Call<pushResponseVo> rtn = agentAPIInfo.requestGetVC(token);
        rtn.enqueue(new Callback<pushResponseVo>(){
            @Override
            public void onResponse(@NotNull Call<pushResponseVo> call, @NotNull Response<pushResponseVo> response){
                if(response.code() == 200){
                    if(result != null){
                        result.setValue(response.body());
                    }
                }else{
                    if(response.errorBody() != null){
                        String errorMessage;
                        try{
                            errorMessage = response.errorBody().string();
                        }catch(IOException e){
                            e.printStackTrace();
                        }
                    }
                }
            }

            @Override
            public void onFailure(@NotNull Call<pushResponseVo> call, @NotNull Throwable t){
                if(result != null){
                    result.setValue(null);
                }
                PrintLog.crash("onFailure" + t.getMessage());
            }
        });
    }

}
