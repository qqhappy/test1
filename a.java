    package cn.com.file;  
      
    import <a href="http://lib.csdn.net/base/java" class='replace_word' title="Java ֪ʶ��" target='_blank' style='color:#df3434; font-weight:bold;'>Java</a>.io.ByteArrayOutputStream;  
    import java.io.FileInputStream;  
    import java.io.FileNotFoundException;  
    import java.io.FileOutputStream;  
    import java.io.IOException;  
      
    import android.app.Activity;  
    import android.os.Bundle;  
    import android.view.View;  
    import android.view.View.OnClickListener;  
    import android.widget.Button;  
    import android.widget.EditText;  
    import android.widget.TextView;  
    import android.widget.Toast;  
      
    public class FileTest extends Activity {  
        private EditText editText;  
        private TextView showTextView;  
        // Ҫ������ļ���  
        private String fileName = "chenzheng_java.txt";  
      
        @Override  
        public void onCreate(Bundle savedInstanceState) {  
            super.onCreate(savedInstanceState);  
            setContentView(R.layout.main);  
            // ��ȡҳ���е����  
            editText = (EditText) findViewById(R.id.addText);  
            showTextView = (TextView) findViewById(R.id.showText);  
            Button addButton = (Button) this.findViewById(R.id.addButton);  
            Button showButton = (Button) this.findViewById(R.id.showButton);  
            // �󶨵����¼�  
            addButton.setOnClickListener(listener);  
            showButton.setOnClickListener(listener);  
      
        }  
      
        // ����������  
        private View.OnClickListener listener = new OnClickListener() {  
            public void onClick(View v) {  
                Button view = (Button) v;  
                switch (view.getId()) {  
                case R.id.addButton:  
                    save();  
                    break;  
                case R.id.showButton:  
                    read();  
                    break;  
      
                }  
      
            }  
      
        };  
      
        /** 
         *@author chenzheng_Java  
         *�����û���������ݵ��ļ� 
         */  
        private void save() {  
      
            String content = editText.getText().toString();  
            try {  
                /* �����û��ṩ���ļ������Լ��ļ���Ӧ��ģʽ����һ�������.�ļ�����ϵͳ��Ϊ�㴴��һ���ģ� 
                 * ����Ϊʲô����ط�����FileNotFoundException�׳�����Ҳ�Ƚ����ơ���Context������������� 
                 *   public abstract FileOutputStream openFileOutput(String name, int mode) 
                 *   throws FileNotFoundException; 
                 * openFileOutput(String name, int mode); 
                 * ��һ�������������ļ����ƣ�ע��������ļ����Ʋ��ܰ����κε�/����/���ַָ�����ֻ�����ļ��� 
                 *          ���ļ��ᱻ������/data/data/Ӧ������/files/chenzheng_java.txt 
                 * �ڶ��������������ļ��Ĳ���ģʽ 
                 *          MODE_PRIVATE ˽�У�ֻ�ܴ�������Ӧ�÷��ʣ� �ظ�д��ʱ���ļ����� 
                 *          MODE_APPEND  ˽��   �ظ�д��ʱ�����ļ���ĩβ����׷�ӣ������Ǹ��ǵ�ԭ�����ļ� 
                 *          MODE_WORLD_READABLE ����  �ɶ� 
                 *          MODE_WORLD_WRITEABLE ���� �ɶ�д 
                 *  */  
                FileOutputStream outputStream = openFileOutput(fileName,  
                        Activity.MODE_PRIVATE);  
                outputStream.write(content.getBytes());  
                outputStream.flush();  
                outputStream.close();  
                Toast.makeText(FileTest.this, "����ɹ�", Toast.LENGTH_LONG).show();  
            } catch (FileNotFoundException e) {  
                e.printStackTrace();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
      
        }  
      
        /** 
         * @author chenzheng_java  
         * ��ȡ�ղ��û���������� 
         */  
        private void read() {  
            try {  
                FileInputStream inputStream = this.openFileInput(fileName);  
                byte[] bytes = new byte[1024];  
                ByteArrayOutputStream arrayOutputStream = new ByteArrayOutputStream();  
                while (inputStream.read(bytes) != -1) {  
                    arrayOutputStream.write(bytes, 0, bytes.length);  
                }  
                inputStream.close();  
                arrayOutputStream.close();  
                String content = new String(arrayOutputStream.toByteArray());  
                showTextView.setText(content);  
      
            } catch (FileNotFoundException e) {  
                e.printStackTrace();  
            } catch (IOException e) {  
                e.printStackTrace();  
            }  
      
        }  
      
    }  