package com.topmail;

import com.secretlib.util.Log;
import com.topmail.events.*;
import com.topmail.model.Config;
import com.topmail.model.Env;

import javax.imageio.ImageIO;
import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.beans.XMLDecoder;
import java.beans.XMLEncoder;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.text.MessageFormat;
import java.util.Arrays;
import java.util.Iterator;
import java.util.Locale;
import java.util.ResourceBundle;

/**
 * @author Florent FRADET
 */
public class Main extends JFrame implements TopEventListener {

    private static final Log LOG = new Log(Main.class);

    public static final String VERSION = "1.1.0";

    private static ResourceBundle bundle;
    private static Env env = new Env();

    // Parsed arguments only
    private String inputFile = null;


    public void loadConfig() {
        Config cfg = new Config();
        try {
            File f = new File("config.xml");
            XMLDecoder xd = new XMLDecoder(new FileInputStream(f));
            cfg = (Config) xd.readObject();
            env.setCfg(cfg);
            xd.close();

            // Ensure the frame is visible
            // (i.e. cfg has coordinates on a disconnected screen on a multiple-screens desktop)
            boolean bVisible = false;
            GraphicsEnvironment ge = GraphicsEnvironment.getLocalGraphicsEnvironment();
            GraphicsDevice[] screens = ge.getScreenDevices();
            for (GraphicsDevice screen : screens) {
                Rectangle screenBounds = screen.getDefaultConfiguration().getBounds();
                bVisible |= screenBounds.contains(cfg.getFrameRect());
            }
            if (!bVisible) {
                // Reset position to main screen
                cfg.getFrameRect().move(0, 0);
            }
            setBounds(cfg.getFrameRect());
        } catch (Exception e) {
            // NO OP
            cfg.setFrameRect(null);
            cfg.setAlgo(null);
            cfg.setLastOpenDir(".");
        }

        if (cfg.getLang() == null) {
            JDialog dlg = new JDialog();
            dlg.setModal(true);
            dlg.setTitle("Choose your language");
            dlg.setSize(200, 70);
            dlg.setIconImage(getIconImage());
            dlg.setLayout(new BorderLayout());
            JComboBox<Locale> cmb = new JComboBox<>();
            cmb.addItem(Locale.forLanguageTag("fr-FR"));
            cmb.addItem(Locale.forLanguageTag("en-US"));
            cmb.setRenderer(new ListCellRenderer<Locale>() {
                @Override
                public Component getListCellRendererComponent(JList<? extends Locale> list, Locale value, int index, boolean isSelected, boolean cellHasFocus) {
                    return new JLabel(value.getDisplayName());
                }
            });
            dlg.add(cmb, BorderLayout.CENTER);
            JButton btnOk = new JButton("OK");
            btnOk.addActionListener(new ActionListener() {
                @Override
                public void actionPerformed(ActionEvent e) {
                    dlg.dispose();
                }
            });
            dlg.add(btnOk, BorderLayout.EAST);
            dlg.setVisible(true);
            // Modal dlg
            Locale loc = (Locale) cmb.getSelectedItem();
            cfg.setLang(loc.getLanguage() + "-" + loc.getCountry());
        }
        Locale.setDefault(Locale.forLanguageTag(cfg.getLang()));
        try {
            bundle = ResourceBundle.getBundle("messages");
        } catch (Exception e) {
            Locale.setDefault(Locale.forLanguageTag("en-US"));
            bundle = ResourceBundle.getBundle("messages");
        }
    }


    public void saveConfig() {
        File f = new File("config.xml");
        Config cfg = env.getCfg();
        cfg.setFrameRect(getBounds());
        try {
            XMLEncoder xe = new XMLEncoder(new FileOutputStream(f));
            xe.writeObject(cfg);
            xe.close();
        } catch (Exception e) {
            e.printStackTrace();
            // NO OP
        }
    }

    protected void updateTitle(String state) {
        String s = "Top Mail - v" + VERSION;
        if ((state != null) && (state.length() > 0)) {
            s += " - " + state;
        }
        setTitle(s);
    }

    public Main(String[] args) {
        parseArgs(args);

        TopEventDispatcher.addListener(this);

        this.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        try {
            InputStream is = getClass().getClassLoader().getResourceAsStream("favicon64.png");
            this.setIconImage(ImageIO.read(is));
            is.close();
        } catch (Exception e) {
            // NO OP
        }

        updateTitle(null);

        MainPanel mp = new MainPanel(this);
        getContentPane().setLayout(new BorderLayout());
        getContentPane().add(mp, BorderLayout.CENTER);
        setSize(450, 560);
        loadConfig();
        mp.initialize();
        if (inputFile != null) {
            TopEventDispatcher.dispatch(new TopEventInputFileChanged(new File(inputFile)));
        }

        setVisible(true);

        addWindowListener(new WindowAdapter() {
            @Override
            public void windowClosing(WindowEvent e) {
                saveConfig();
                e.getWindow().dispose();
            }
        });
    }


    private void parseArgs(String[] args) {
        Iterator<String> iter = Arrays.stream(args).iterator();
        while (iter.hasNext()) {
            String arg = iter.next();
            if (arg.startsWith("-")) {
                if ("-quiet".equals(arg)) {
                    env.setShowDlg(false);
                } else if ("-tron".equals(arg)) {
                    Log.setLevel(Log.TRACE);
                } else if ("-h".equals(arg)) {
                    if (iter.hasNext()) {
                        String algo = iter.next();
                        env.getCfg().setAlgo(algo);
                        env.getParams().setHashAlgo(algo);
                    }
                } else if ("-pm".equals(arg)) {
                    if (iter.hasNext()) {
                        env.getParams().setKm(iter.next());
                    }
                } else if ("-pd".equals(arg)) {
                    if (iter.hasNext()) {
                        env.getParams().setKd(iter.next());
                    }
                } else {
                    LOG.error("invalid arg : " + arg);
                }
            } else {
                inputFile = arg;
            }
        }

    }


    public static String getString(String key, Object... args) {
        String s = null;
        try {
            s = bundle.getString(key);
        } catch (Exception e) {
            s = "[key : " + key + "]";
        }
        return MessageFormat.format(s, args);
    }


    public static Env getEnv() {
        return env;
    }


    public static void main(String[] args) {
        //Log.setLevel(Log.WARN);

        Log.setLevel(Log.TRACE);

        Main frame = new Main(args);
        frame.toFront();
    }

    @Override
    public void processTopEvent(TopEventBase e) {
        if (e instanceof TopEventProgressStateChanged) {
            TopEventProgressStateChanged ev = (TopEventProgressStateChanged) e;
            updateTitle(ev.getState());
        }
    }
}
