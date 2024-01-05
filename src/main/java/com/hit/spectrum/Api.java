package com.hit.spectrum;

import com.hit.spectrum.data.DataConvertUtils;
import com.hit.spectrum.data.SpectrumData;
import com.hit.spectrum.data.SpectrumDbData;
import com.hit.spectrum.service.SpectrumService;

import java.util.List;

public class Api {

    /**
     * 标准品入库接口
     * @param name 标准品名称
     * @param data 标准品原始光谱
     * @return
     */
    public boolean input(String name, List<Double> data){
        return SpectrumService.enterStandard(name, data);
    }


    /**
     * 混合物识别接口
     * @param data 混合物原始光谱
     * @return
     */
    public List<String> iden(List<Double> data){
        return SpectrumService.identification(data);
    }

    /**
     * 返回所有纯净物名称和id
     * @return
     */
    public List<SpectrumData> getAll(){
        return SpectrumService.getAll();
    }

    /**
     * 根据id获取某一个纯净物的数据
     * @return
     */
    public SpectrumData getStandardById(Long id){
        return SpectrumService.getStandardById(id);
    }


    public boolean deleteById(Long id){
        return SpectrumService.deleteById(id);
    }
}
