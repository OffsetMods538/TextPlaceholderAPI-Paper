package eu.pb4.placeholderstest;

import com.mojang.brigadier.arguments.StringArgumentType;
import com.mojang.brigadier.context.CommandContext;
import eu.pb4.placeholders.api.ParserContext;
import eu.pb4.placeholders.api.ServerPlaceholderContext;
import eu.pb4.placeholders.api.Placeholders;
import eu.pb4.placeholders.api.arguments.StringArgs;
import eu.pb4.placeholders.api.node.LiteralNode;
import eu.pb4.placeholders.api.node.TextNode;
import eu.pb4.placeholders.api.parsers.*;
import io.papermc.paper.command.brigadier.CommandSourceStack;
import io.papermc.paper.plugin.lifecycle.event.types.LifecycleEvents;
import it.unimi.dsi.fastutil.Pair;
import net.kyori.adventure.text.Component;
import net.kyori.adventure.text.serializer.json.JSONComponentSerializer;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;

import static io.papermc.paper.command.brigadier.Commands.argument;
import static io.papermc.paper.command.brigadier.Commands.literal;


@SuppressWarnings("deprecation")
public class TestMod extends JavaPlugin {
    private static int perf(CommandContext<CommandSourceStack> context) {
        var input = context.getArgument("component", String.class);
        Player player = (Player) context.getSource().getExecutor();
        int iter = 1024 * 20;
        // old = NodeParser.merge(TextParserV1.DEFAULT, MarkdownLiteParserV1.ALL, LegacyFormattingParser.ALL)

        for (var pair : List.of(
                Pair.of(TagParser.SIMPLIFIED_TEXT_FORMAT, TagLikeParser.of(TagLikeParser.PLACEHOLDER,
                        TagLikeParser.Provider.placeholder(ServerPlaceholderContext.SERVER_KEY, Placeholders.SERVER_PLACEHOLDER_GETTER))),
                Pair.of(NodeParser.merge(TagParser.SIMPLIFIED_TEXT_FORMAT, TagLikeParser.of(TagLikeParser.PLACEHOLDER,
                        TagLikeParser.Provider.placeholder(ServerPlaceholderContext.SERVER_KEY, Placeholders.SERVER_PLACEHOLDER_GETTER))), NodeParser.NOOP)
        )) {
            player.sendMessage(Component.text("Parser: " + pair));
            long placeholderTimeTotal = 0;
            long contextTimeTotal = 0;
            long tagTimeTotal = 0;
            long textTimeTotal = 0;
            Component output = null;

            var parser = pair.left();
            var placeholder = pair.right();

            try {
                for (int i = 0; i < iter; i++) {
                    var time = System.nanoTime();
                    var tags = TextNode.asSingle(parser.parseNodes(new LiteralNode(input)));
                    tagTimeTotal += System.nanoTime() - time;
                    time = System.nanoTime();

                    var placeholders = TextNode.asSingle(placeholder.parseNodes(tags));
                    placeholderTimeTotal += System.nanoTime() - time;
                    time = System.nanoTime();

                    var ctx = ParserContext.of(ServerPlaceholderContext.SERVER_KEY, ServerPlaceholderContext.of(player));
                    contextTimeTotal += System.nanoTime() - time;
                    time = System.nanoTime();

                    Component text = placeholders.toAdventureComponent(ctx, true);
                    textTimeTotal += System.nanoTime() - time;
                    output = text;
                }
                long total = tagTimeTotal + placeholderTimeTotal + textTimeTotal + contextTimeTotal;

                //player.sendMessage(Text.literal(toJsonString(output)), false);
                // no clue what this was: player.sendMessage(PaperAdventure.asAdventure(ComponentUtils.resolve(ResolutionContext.create((net.minecraft.commands.CommandSourceStack) context.getSource()), new AdventureComponent(output).deepConverted())));
                player.sendMessage(output);
                player.sendMessage(Component.text(
                        "<FULL> Tag: " + ((tagTimeTotal / 1000) / 1000d) + " ms | " +
                                "Context: " + ((contextTimeTotal / 1000) / 1000d) + " ms | " +
                                "Placeholder: " + ((placeholderTimeTotal / 1000) / 1000d) + " ms | " +
                                "Text: " + ((textTimeTotal / 1000) / 1000d) + " ms | " +
                                "All: " + ((total / 1000) / 1000d) + " ms"
                ));

                player.sendMessage(Component.text(
                        "<SINGLE> Tag: " + ((tagTimeTotal / iter / 1000) / 1000d) + " ms | " +
                                "Context: " + ((contextTimeTotal / iter / 1000) / 1000d) + " ms | " +
                                "Placeholder: " + ((placeholderTimeTotal / iter / 1000) / 1000d) + " ms | " +
                                "Text: " + ((textTimeTotal / iter / 1000) / 1000d) + " ms | " +
                                "All: " + ((total / iter / 1000) / 1000d) + " ms"
                ));
            } catch (Exception e) {
                e.printStackTrace();
            }
        }
        return 0;
    }

    private static int argTest(CommandContext<CommandSourceStack> context) {
        context.getSource().getExecutor().sendMessage(Component.text(
                StringArgs.full(context.getArgument("arg", String.class), ' ', ':').toString()));
        return 0;
    }

    private static int markqt(CommandContext<CommandSourceStack> context) {
        try {
            Player player = (Player) context.getSource().getExecutor();
            player.sendMessage(NodeParser.builder().markdown().quickText().build().parseAdventureComponent(context.getArgument("component", String.class), ParserContext.of()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static String toJsonString(Component text) {
        return JSONComponentSerializer.json().serialize(text);
    }

    private static int test3(CommandContext<CommandSourceStack> context) {
        try {
            Player player = (Player) context.getSource().getExecutor();
            var time = System.nanoTime();
            var tags = TextNode.asSingle(
                    LegacyFormattingParser.ALL.parseNodes(
                            TextNode.asSingle(
                                    MarkdownLiteParserV1.ALL.parseNodes(
                                            TextNode.asSingle(
                                                    TagParser.DEFAULT.parseNodes(new LiteralNode(context.getArgument("component", String.class)))
                                            )
                                    )
                            )
                    )
            );
            var tagTime = System.nanoTime() - time;
            time = System.nanoTime();

            //var placeholders = Placeholders.parseNodes(tags);
            var placeholderTime = System.nanoTime() - time;
            time = System.nanoTime();

            //Component text = placeholders.toAdventureComponent(ParserContext.of(ServerPlaceholderContext.SERVER_KEY, ServerPlaceholderContext.of(player)), true);
            var textTime = System.nanoTime() - time;

            //player.sendSystemMessage(Component.literal(toJsonString(text, context.getSource().registryAccess())), false);
            //player.sendSystemMessage(ComponentUtils.updateForEntity(context.getSource(), text, context.getSource().getEntity(), 0), false);
            player.sendMessage(Component.text(
                      "Tag: " + ((tagTime / 1000) / 1000d) + " ms | " +
                            "Placeholder: " + ((placeholderTime / 1000) / 1000d) + " ms | " +
                            "Text: " + ((textTime / 1000) / 1000d) + " ms | " +
                            "All: " + (((tagTime + placeholderTime + textTime) / 1000) / 1000d) + " ms"
                    ));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test5(CommandContext<CommandSourceStack> context) {
        try {
            Player player = (Player) context.getSource().getExecutor();
            var form = context.getArgument("component", String.class);

            Component text2 = NodeParser.builder()
                    .serverPlaceholders()
                    .simplifiedTextFormat()
                    .build()
                    .parseAdventureComponent(form, ServerPlaceholderContext.of(player).asParserContext());
            player.sendMessage(Component.text(toJsonString(text2)));
            player.sendMessage(text2);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test6x(CommandContext<CommandSourceStack> context) {
        try {
            Player player = (Player) context.getSource().getExecutor();
            ParserContext parsingContext = ParserContext.of();
            //parsingContext.with(ParserContext.Key.HOLDER_LOOKUP, player.registryAccess());     // You need to use this for Hover Item to work TODO: with paper maybe not?
            var form = context.getArgument("component", String.class);
            player.sendMessage(Component.text("------------------------------"));
            player.sendMessage(Component.text("Input.   | " + form));
            player.sendMessage(Component.text("STF-V2 | ").append(TagParser.SIMPLIFIED_TEXT_FORMAT.parseAdventureComponent(form, parsingContext)));
            player.sendMessage(Component.text("STF+QT | ").append(TagParser.QUICK_TEXT_WITH_STF.parseAdventureComponent(form, parsingContext)));
            player.sendMessage(Component.text("QT       | ").append(TagParser.QUICK_TEXT.parseAdventureComponent(form, parsingContext)));
        } catch (Throwable e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test7(CommandContext<CommandSourceStack> context) {
        try {
            Player player = (Player) context.getSource().getExecutor();

            //var text = Placeholders.parseComponent(Component.translatable("death.attack.outOfWorld", player.getDisplayName()), ServerPlaceholderContext.of(player));
            //player.sendSystemMessage(text);
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    private static int test8(CommandContext<CommandSourceStack> context) {
        try {
            var parser = NodeParser.builder().quickText().serverPlaceholders().build();
            context.getSource().getSender().sendMessage(parser.parseAdventureComponent(StringArgumentType.getString(context, "component"), ServerPlaceholderContext.of(context.getSource()).asParserContext()));
        } catch (Exception e) {
            e.printStackTrace();
        }
        return 0;
    }

    @Override
    public void onEnable() {
        record ExampleClass(int n) {}
        
        var a = new ExampleClass(5);
        var b = new ExampleClass(5);
        
        System.out.println(a == b);
        System.out.println(a.equals(b));
        System.out.println(a.hashCode() == b.hashCode());


        this.getLifecycleManager().registerEventHandler(LifecycleEvents.COMMANDS, commands -> {
            /*dispatcher.register(
                    literal("test").then(argument("component", ComponentArgument.textComponent(registryAccess)).executes(TestMod::test))
            );*/
            commands.registrar().register(
                    literal("argtest").then(argument("arg", StringArgumentType.greedyString()).executes(TestMod::argTest)).build()
            );



            commands.registrar().register(
                    literal("test3").then(argument("component", StringArgumentType.greedyString()).executes(TestMod::test3)).build()
            );

            commands.registrar().register(
                    literal("perm").then(argument("component", StringArgumentType.greedyString()).executes(TestMod::perf)).build()
            );

            commands.registrar().register(
                    literal("test5").then(argument("component", StringArgumentType.greedyString()).executes(TestMod::test5)).build()
            );

            commands.registrar().register(
                    literal("test6ohno").then(argument("component", StringArgumentType.greedyString()).executes(TestMod::test6x)).build()
            );

            commands.registrar().register(
                    literal("test7").executes(TestMod::test7).build()
            );

            commands.registrar().register(
                    literal("test8").then(argument("component", StringArgumentType.greedyString()).executes(TestMod::test8)).build()
            );

            commands.registrar().register(
                    literal("markqt").then(argument("component", StringArgumentType.greedyString()).executes(TestMod::markqt)).build()
            );

            commands.registrar().register(
                    literal("whowhereandwhenami").executes(context -> {
                        context.getSource().getSender().sendMessage(NodeParser.builder().serverPlaceholders().quickText().build().parseAdventureComponent(
                                        """
                                        <rb>Hello world!</>
                                        You are %player:head% %player:name%
                                        <gr yellow gold>Position: %player:pos_x% %player:pos_y% %player:pos_z% in %player:biome%</>
                                        Time: %world:time%
                                        """, ServerPlaceholderContext.of(context.getSource()).asParserContext()));
                        return 1;
                    }).build()
            );
        });
    }
}
