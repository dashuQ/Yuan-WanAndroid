package com.example.yuan_wanandroid.presenter.system;

import com.example.yuan_wanandroid.base.BaseObserver;
import com.example.yuan_wanandroid.base.presenter.BasePresenter;
import com.example.yuan_wanandroid.contract.system.SystemArticlesFragmentContract;
import com.example.yuan_wanandroid.model.DataModel;
import com.example.yuan_wanandroid.model.entity.Articles;
import com.example.yuan_wanandroid.model.entity.BaseResponse;
import com.example.yuan_wanandroid.utils.RxUtil;

import javax.inject.Inject;

/**
 * <pre>
 *     author : 残渊
 *     time   : 2019/01/23
 *     desc   :
 * </pre>
 */


public class SystemArticlesFragmentPresenter extends BasePresenter<SystemArticlesFragmentContract.View>
        implements SystemArticlesFragmentContract.Presenter {


    @Inject
    public SystemArticlesFragmentPresenter(DataModel model) {
        super(model);
    }

    @Override
    public void loadSystemArticlesData(int pageNum, int id) {
        addRxSubscribe(
                mModel.getSecondSystemArticles(pageNum, id)
                        .compose(RxUtil.rxSchedulerHelper())
                        .compose(RxUtil.handleResult())
                        .subscribeWith(new BaseObserver<Articles>(mView) {
                            @Override
                            public void onNext(Articles articles) {
                                super.onNext(articles);
                                mView.showSystemArticles(articles.getDatas());
                            }
                        })
        );
    }

    @Override
    public void loadMoreSystemArticlesData(int pageNum, int id) {
        addRxSubscribe(
                mModel.getSecondSystemArticles(pageNum, id)
                        .compose(RxUtil.rxSchedulerHelper())
                        .compose(RxUtil.handleResult())
                        .subscribeWith(new BaseObserver<Articles>(mView, false, false) {
                            @Override
                            public void onNext(Articles articles) {
                                super.onNext(articles);
                                mView.showMoreSystemArticles(articles.getDatas());
                            }
                        })
        );
    }

    @Override
    public void collectArticles(int id) {
        addRxSubscribe(
                mModel.collectArticles(id)
                        .compose(RxUtil.rxSchedulerHelper())
                        .subscribeWith(new BaseObserver<BaseResponse>(mView, false, false) {
                            @Override
                            public void onNext(BaseResponse baseResponse) {
                                mView.showCollectSuccess();
                            }
                        })
        );
    }

    @Override
    public void unCollectArticles(int id) {
        addRxSubscribe(
                mModel.unCollectArticles(id)
                        .compose(RxUtil.rxSchedulerHelper())
                        .subscribeWith(new BaseObserver<BaseResponse>(mView, false, false) {
                            @Override
                            public void onNext(BaseResponse baseResponse) {
                                mView.showUnCollectSuccess();
                            }
                        })
        );
    }
}
