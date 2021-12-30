# StructureApi

All URIs are relative to *http://localhost:8080/api*

Method | HTTP request | Description
------------- | ------------- | -------------
[**createNewStructure**](StructureApi.md#createNewStructure) | **POST** /v2/structure | Create new structure
[**deleteStructureByStructureId**](StructureApi.md#deleteStructureByStructureId) | **DELETE** /v2/structure/{structureId} | Delete structure by id
[**getStructureById**](StructureApi.md#getStructureById) | **GET** /v2/structure/{structureId} | Get structure by id
[**updateStructure**](StructureApi.md#updateStructure) | **PUT** /v2/structure | Update structure

<a name="createNewStructure"></a>
# **createNewStructure**
> createNewStructure(body)

Create new structure

Create a new structure

### Example
```java
// Import classes:
//import fr.ans.psc.ApiException;
//import fr.ans.psc.api.StructureApi;


StructureApi apiInstance = new StructureApi();
Structure body = new Structure(); // Structure | 
try {
    apiInstance.createNewStructure(body);
} catch (ApiException e) {
    System.err.println("Exception when calling StructureApi#createNewStructure");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Structure**](Structure.md)|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

<a name="deleteStructureByStructureId"></a>
# **deleteStructureByStructureId**
> deleteStructureByStructureId(structureId)

Delete structure by id

Delete structure by structureId

### Example
```java
// Import classes:
//import fr.ans.psc.ApiException;
//import fr.ans.psc.api.StructureApi;


StructureApi apiInstance = new StructureApi();
String structureId = "structureId_example"; // String | 
try {
    apiInstance.deleteStructureByStructureId(structureId);
} catch (ApiException e) {
    System.err.println("Exception when calling StructureApi#deleteStructureByStructureId");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **structureId** | **String**|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="getStructureById"></a>
# **getStructureById**
> Structure getStructureById(structureId)

Get structure by id

Get structure by id

### Example
```java
// Import classes:
//import fr.ans.psc.ApiException;
//import fr.ans.psc.api.StructureApi;


StructureApi apiInstance = new StructureApi();
String structureId = "structureId_example"; // String | 
try {
    Structure result = apiInstance.getStructureById(structureId);
    System.out.println(result);
} catch (ApiException e) {
    System.err.println("Exception when calling StructureApi#getStructureById");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **structureId** | **String**|  |

### Return type

[**Structure**](Structure.md)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: Not defined
 - **Accept**: application/json

<a name="updateStructure"></a>
# **updateStructure**
> updateStructure(body)

Update structure

Update structure

### Example
```java
// Import classes:
//import fr.ans.psc.ApiException;
//import fr.ans.psc.api.StructureApi;


StructureApi apiInstance = new StructureApi();
Structure body = new Structure(); // Structure | 
try {
    apiInstance.updateStructure(body);
} catch (ApiException e) {
    System.err.println("Exception when calling StructureApi#updateStructure");
    e.printStackTrace();
}
```

### Parameters

Name | Type | Description  | Notes
------------- | ------------- | ------------- | -------------
 **body** | [**Structure**](Structure.md)|  |

### Return type

null (empty response body)

### Authorization

No authorization required

### HTTP request headers

 - **Content-Type**: application/json
 - **Accept**: application/json

