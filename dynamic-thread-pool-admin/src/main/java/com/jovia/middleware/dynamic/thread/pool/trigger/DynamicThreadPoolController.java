package com.jovia.middleware.dynamic.thread.pool.trigger;

import com.jovia.middleware.dynamic.thread.pool.sdk.domain.model.ThreadPoolConfigEntity;
import com.jovia.middleware.dynamic.thread.pool.sdk.domain.valobj.RegistryEnumVO;
import com.jovia.middleware.dynamic.thread.pool.type.Response;
import com.jovia.middleware.dynamic.thread.pool.type.ResponseStatusEnum;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.redisson.Redisson;
import org.redisson.api.RList;
import org.springframework.web.bind.annotation.CrossOrigin;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author Jay
 * @date 2025-10-20-23:04
 */
@Slf4j
@RestController
@CrossOrigin("*")
@RequestMapping("/api/dynamic_thread_pool/")
public class DynamicThreadPoolController {


    @Resource
    private Redisson redisson;
    
    @GetMapping(value = "query_thread_pool_list")
    public Response queryThreadPoolList(){
        try{
            RList<ThreadPoolConfigEntity> list = redisson.getList(RegistryEnumVO.THREAD_POOL_CONFIG_PARAMETER_LIST_KEY.getKey());   
            return new Response(ResponseStatusEnum.OK).addData("data", list);
        }catch (Exception e){
            log.error("查询线程池数据异常:{}", e.getMessage());
            return Response.error("查询线程池数据异常");
        }
    }
}
