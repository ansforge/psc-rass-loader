# PsApi

All URIs are relative to *http://localhost:8080/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createNewPs**](PsApi.md#createNewPs) | **POST** /v2/ps | Create new Ps
[**deletePsById**](PsApi.md#deletePsById) | **DELETE** /v2/ps/{psId} | Delete Ps by id
[**forceDeletePsById**](PsApi.md#forceDeletePsById) | **DELETE** /v2/ps/force/{psId} | Physical delete of Ps
[**getPsById**](PsApi.md#getPsById) | **GET** /v2/ps/{psId} | Get Ps by id
[**updatePs**](PsApi.md#updatePs) | **PUT** /v2/ps | Update Ps

<a name="createNewPs"></a>
# **createNewPs**
> createNewPs(body)

Create new Ps

Create a new Ps

### Example
```java
// Import classes:
//import fr.ans.psc.ApiException;
//import fr.ans.psc.api.PsApi;


PsApi apiInstance = new PsApi();
Ps body = new Ps(); // Ps | The Ps to be created
try {
    apiInstance.createNewPs(body);
} catch (ApiException e) {
    System.err.println("Exception when calling PsApi#createNewPs");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Ps**](Ps.md)| The Ps to be created |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="deletePsById"></a>
# **deletePsById**
> deletePsById(psId)

Delete Ps by id

Delete a PS by its id

### Example
```java
// Import classes:
//import fr.ans.psc.ApiException;
//import fr.ans.psc.api.PsApi;


PsApi apiInstance = new PsApi();
String psId = "psId_example"; // String | 
try {
    apiInstance.deletePsById(psId);
} catch (ApiException e) {
    System.err.println("Exception when calling PsApi#deletePsById");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **psId** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="forceDeletePsById"></a>
# **forceDeletePsById**
> forceDeletePsById(psId)

Physical delete of Ps

completely delete Ps (not only deactivate it)

### Example
```java
// Import classes:
//import fr.ans.psc.ApiException;
//import fr.ans.psc.api.PsApi;


PsApi apiInstance = new PsApi();
String psId = "psId_example"; // String | 
try {
    apiInstance.forceDeletePsById(psId);
} catch (ApiException e) {
    System.err.println("Exception when calling PsApi#forceDeletePsById");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **psId** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="getPsById"></a>
# **getPsById**
> Ps getPsById(psId)

Get Ps by id

get a Ps by one of its idNationalRef

### Example
```java
// Import classes:
//import fr.ans.psc.ApiException;
//import fr.ans.psc.api.PsApi;


PsApi apiInstance = new PsApi();
String psId = "psId_example"; // String | 
try {
    Ps result = apiInstance.getPsById(psId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling PsApi#getPsById");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **psId** | **String**|  |

### Return type

[**Ps**](Ps.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="updatePs"></a>
# **updatePs**
> updatePs(body)

Update Ps

Update Ps

### Example
```java
// Import classes:
//import fr.ans.psc.ApiException;
//import fr.ans.psc.api.PsApi;


PsApi apiInstance = new PsApi();
Ps body = new Ps(); // Ps | 
try {
    apiInstance.updatePs(body);
} catch (ApiException e) {
    System.err.println("Exception when calling PsApi#updatePs");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Ps**](Ps.md)|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

