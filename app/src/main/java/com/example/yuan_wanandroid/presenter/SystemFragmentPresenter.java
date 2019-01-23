package com.example.yuan_wanandroid.presenter;

import com.example.yuan_wanandroid.base.BaseObserver;
import com.example.yuan_wanandroid.base.presenter.BasePresenter;
import com.example.yuan_wanandroid.contract.SystemFragmentContract;
import com.example.yuan_wanandroid.model.DataModel;
import com.example.yuan_wanandroid.model.entity.FirstSystem;
import com.example.yuan_wanandroid.utils.LogUtil;
import com.example.yuan_wanandroid.utils.RxUtil;

import java.util.List;

import javax.inject.Inject;

/**
 * <pre>
 *     author : 残渊
 *     time   : 2019/01/23
 *     desc   :
 * </pre>
 */


public class SystemFragmentPresenter extends BasePresenter<SystemFragmentContract.View>
        implements SystemFragmentContract.Presenter {

    @Inject
    public SystemFragmentPresenter(DataModel model) {
        super(model);
    }

    @Override
    public void loadSystemData() {
        addRxSubscribe(mModel.getFirstSystemData()
                .compose(RxUtil.rxSchedulerHelper())
                .compose(RxUtil.handleResult())
                .subscribeWith(new BaseObserver<List<FirstSystem>>(mView,false,false){
                    @Override
                    public void onNext(List<FirstSystem> firstSystemList){
                        super.onNext(firstSystemList);
                        mView.showSystemData(firstSystemList);
                    }
                }));

    }
}
