package com.fstyle.structure_android.screen.main;

import android.content.DialogInterface;
import android.os.Bundle;
import android.os.Parcelable;
import android.support.design.widget.TextInputLayout;
import android.view.View;
import android.widget.EditText;
import com.fstyle.structure_android.R;
import com.fstyle.structure_android.data.model.User;
import com.fstyle.structure_android.data.source.UserRepositoryImpl;
import com.fstyle.structure_android.data.source.remote.UserRemoteDataSource;
import com.fstyle.structure_android.data.source.remote.api.service.NameServiceClient;
import com.fstyle.structure_android.screen.BaseActivity;
import com.fstyle.structure_android.screen.searchresult.SearchResultActivity;
import com.fstyle.structure_android.utils.navigator.Navigator;
import com.fstyle.structure_android.utils.rx.CustomCompositeSubscription;
import com.fstyle.structure_android.utils.rx.SchedulerProvider;
import com.fstyle.structure_android.utils.validator.Rule;
import com.fstyle.structure_android.utils.validator.ValidType;
import com.fstyle.structure_android.utils.validator.Validation;
import com.fstyle.structure_android.utils.validator.Validator;
import com.fstyle.structure_android.widget.dialog.DialogManager;
import com.fstyle.structure_android.widget.dialog.DialogManagerImpl;
import java.util.ArrayList;
import java.util.List;

import static com.fstyle.structure_android.utils.Constant.LIST_USER_ARGS;

/**
 * Create by DaoLQ
 */
public class MainActivity extends BaseActivity implements MainContract.View {
    private static final String TAG = MainActivity.class.getName();

    private MainContract.Presenter mPresenter;

    private TextInputLayout mTextInputLayoutKeyword;
    private EditText mEditTextKeyword;
    private TextInputLayout mTextInputLayoutNumberLimit;
    private EditText mEditNumberLimit;

    private DialogManager mDialogManager;

    @Validation({
            @Rule(types = ValidType.NG_WORD, message = R.string.error_unusable_characters),
            @Rule(types = ValidType.NON_EMPTY, message = R.string.must_not_empty)
    })
    private String mKeyWord;
    @Validation({
            @Rule(types = ValidType.VALUE_RANGE_0_100, message = R.string
                    .error_lenght_from_0_to_100),
            @Rule(types = ValidType.NON_EMPTY, message = R.string.must_not_empty)
    })
    private String mLimit;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        UserRepositoryImpl userRepository = new UserRepositoryImpl(null,
                new UserRemoteDataSource(NameServiceClient.getInstance()));
        Validator validator = new Validator(getApplicationContext(), this);
        mPresenter = new MainPresenter(userRepository, validator, new CustomCompositeSubscription(),
                SchedulerProvider.getInstance());
        mPresenter.setView(this);

        mTextInputLayoutKeyword = (TextInputLayout) findViewById(R.id.txtInputLayoutKeyword);
        mEditTextKeyword = (EditText) findViewById(R.id.edtKeyword);
        mTextInputLayoutNumberLimit =
                (TextInputLayout) findViewById(R.id.txtInputLayoutNumberLimit);
        mEditNumberLimit = (EditText) findViewById(R.id.edtNumberLimit);

        mDialogManager = new DialogManagerImpl(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        mPresenter.onStart();
    }

    @Override
    protected void onStop() {
        mPresenter.onStop();
        super.onStop();
    }

    @Override
    public void onSearchError(Throwable throwable) {
        mDialogManager.dialogMainStyle(throwable.getMessage(),
                new DialogInterface.OnClickListener() {
                    @Override
                    public void onClick(DialogInterface dialog, int which) {
                        dialog.dismiss();
                    }
                });
    }

    @Override
    public void onSearchUsersSuccess(List<User> users) {
        Bundle bundle = new Bundle();
        bundle.putParcelableArrayList(LIST_USER_ARGS, (ArrayList<? extends Parcelable>) users);
        new Navigator(this).startActivity(SearchResultActivity.class, bundle);
    }

    @Override
    public void onInvalidKeyWord(String errorMsg) {
        mTextInputLayoutKeyword.setError(errorMsg);
    }

    @Override
    public void onInvalidLimitNumber(String errorMsg) {
        mTextInputLayoutNumberLimit.setError(errorMsg);
    }

    public void onSearchButtonClicked(View view) {
        mKeyWord = mEditTextKeyword.getText().toString().trim();
        mLimit = mEditNumberLimit.getText().toString().trim();

        if (!mPresenter.validateDataInput(mKeyWord, mLimit)) {
            return;
        }
        mPresenter.searchUsers(Integer.parseInt(mLimit), mKeyWord);
    }
}